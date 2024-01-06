package pl.edu.amu.wmi.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.amu.wmi.util.TestUtil.createUserData;


@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Test
    void generateRefreshJwtCookie_successful() throws Exception {
        //given
        String indexNumber = "s123456";
        String jwtSecret = "===========================T=E=S=T==============================";
        String cookieName = "accessToken";
        UserData userData = createUserData(indexNumber, List.of(UserRole.COORDINATOR));
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000, Integer.class);
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret, String.class);
        ReflectionTestUtils.setField(jwtUtils, "jwtCookie", cookieName, String.class);
        //when
        ResponseCookie responseCookie = jwtUtils.generateJwtCookie(userData);
        //then
        assertThat(responseCookie).isNotNull();
        assertThat(responseCookie.getName()).isEqualTo(cookieName);
        assertThat(responseCookie.getValue()).isNotEmpty();
    }

    @Test
    void getCleanJwtCookie_successful() throws Exception {
        //given
        String cookieName = "accessToken";
        ReflectionTestUtils.setField(jwtUtils, "jwtCookie", cookieName, String.class);
        //when
        ResponseCookie cleanJwtCookie = jwtUtils.getCleanJwtCookie();
        //then
        assertThat(cleanJwtCookie).isNotNull();
        assertThat(cleanJwtCookie.getName()).isEqualTo(cookieName);
        assertThat(cleanJwtCookie.getValue()).isEmpty();
    }

    @Test
    void getCleanJwtRefreshCookie_successful() throws Exception {
        //given
        String cookieName = "refreshToken";
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshCookie", cookieName, String.class);
        //when
        ResponseCookie cleanJwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();
        //then
        assertThat(cleanJwtRefreshCookie).isNotNull();
        assertThat(cleanJwtRefreshCookie.getName()).isEqualTo(cookieName);
        assertThat(cleanJwtRefreshCookie.getValue()).isEmpty();
    }

}
