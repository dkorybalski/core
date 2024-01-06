package pl.edu.amu.wmi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import pl.edu.amu.wmi.security.JwtUtils;
import pl.edu.amu.wmi.security.UserDetailsImpl;
import pl.edu.amu.wmi.service.RefreshTokenService;

import java.util.HashSet;

import static pl.edu.amu.wmi.util.TestUtil.ACCESS_TOKEN_COOKIE_NAME;
import static pl.edu.amu.wmi.util.TestUtil.createCookie;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void createAccessTokenCookie() {
        //given
        String indexNumber = "s123456";
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, indexNumber, "test@test.com", new HashSet<>());

        Mockito.when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(createCookie(ACCESS_TOKEN_COOKIE_NAME));
        //when
        authService.createAccessTokenCookie(userDetails);
        //then
        Mockito.verify(jwtUtils).generateJwtCookie(userDetails);
    }

}
