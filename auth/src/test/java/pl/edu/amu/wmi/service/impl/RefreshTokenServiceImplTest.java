package pl.edu.amu.wmi.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.amu.wmi.dao.RefreshTokenDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.RefreshToken;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.exception.TokenRefreshException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static pl.edu.amu.wmi.util.TestUtil.createUserData;


@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenDAO refreshTokenDAO;

    @Mock
    private UserDataDAO userDataDAO;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void createRefreshToken_successful() throws Exception {
        //given
        Long userId = 1L;
        UserData userData = new UserData();
        userData.setId(userId);
        Mockito.when(userDataDAO.findById(userId)).thenReturn(Optional.of(userData));
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L, Long.class);
        //when
        refreshTokenService.createRefreshToken(userId);
        //then
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        Mockito.verify(userDataDAO).findById(userId);
        Mockito.verify(refreshTokenDAO).save(captor.capture());

        RefreshToken refreshToken = captor.getValue();
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getUser()).isEqualTo(userData);
        assertThat(refreshToken.getToken()).isNotNull();
        assertThat(refreshToken.getExpiryDate()).isNotNull();
    }

    @Test
    void createRefreshToken_userDataIsNull_negative() throws Exception {
        //given
        Long userId = 1L;
        Mockito.when(userDataDAO.findById(userId)).thenReturn(Optional.ofNullable(null));
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L, Long.class);
        //when
        assertThrows(BusinessException.class, () -> refreshTokenService.createRefreshToken(userId));
        //then
        Mockito.verify(userDataDAO).findById(userId);
        Mockito.verify(refreshTokenDAO, Mockito.times(0)).save(any());
    }

    @Test
    void verifyExpiration_expiryDateInTheFuture_successful() {
        //given
        RefreshToken refreshToken = new RefreshToken();
        Instant currentInstant = Instant.now();
        Duration duration = Duration.ofDays(1);
        refreshToken.setExpiryDate(currentInstant.plus(duration));
        //when
        RefreshToken returnedRefreshToken = refreshTokenService.verifyExpiration(refreshToken);
        //then
        Mockito.verify(refreshTokenDAO, Mockito.times(0)).delete(refreshToken);

        assertThat(returnedRefreshToken).isNotNull();
        assertThat(returnedRefreshToken).isEqualTo(refreshToken);
    }

    @Test
    void verifyExpiration_expiryDateInThePast_negative() {
        //given
        RefreshToken refreshToken = new RefreshToken();
        Instant currentInstant = Instant.now();
        Duration duration = Duration.ofDays(1);
        refreshToken.setExpiryDate(currentInstant.minus(duration));
        //when
        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyExpiration(refreshToken));
        //then
        Mockito.verify(refreshTokenDAO, Mockito.times(1)).delete(refreshToken);
    }

    @Test
    void deleteByUserId_successful() {
        //given
        String indexNumber = "s123456";
        Long userId = 1L;
        UserData userData = createUserData(indexNumber, List.of(UserRole.COORDINATOR));
        Mockito.when(userDataDAO.findById(userId)).thenReturn(Optional.of(userData));
        //when
        refreshTokenService.deleteByUserId(userId);
        //then
        Mockito.verify(userDataDAO, Mockito.times(1)).findById(userId);
        Mockito.verify(refreshTokenDAO, Mockito.times(1)).deleteByUser(userData);
    }

    @Test
    void deleteByUserId_userNotFound_negative() {
        //given
        Long userId = 1L;
        Mockito.when(userDataDAO.findById(userId)).thenReturn(Optional.ofNullable(null));
        //when
        assertThrows(BusinessException.class, () -> refreshTokenService.deleteByUserId(userId));
        //then
        Mockito.verify(userDataDAO, Mockito.times(1)).findById(userId);
        Mockito.verify(refreshTokenDAO, Mockito.times(0)).deleteByUser(any());
    }

}
