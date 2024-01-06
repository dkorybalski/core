package pl.edu.amu.wmi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.model.LoginRequest;
import pl.edu.amu.wmi.model.UserInfoResponse;
import pl.edu.amu.wmi.security.JwtUtils;
import pl.edu.amu.wmi.security.UserDetailsImpl;
import pl.edu.amu.wmi.service.AuthService;
import pl.edu.amu.wmi.service.RefreshTokenService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.edu.amu.wmi.util.TestUtil.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void authenticateUser_successful() {
        //given
        String indexNumber = "s123456";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(indexNumber);
        Set<GrantedAuthority> authorities = createAuthorities(List.of(UserRole.COORDINATOR, UserRole.SUPERVISOR));
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, indexNumber, "test@test.com", authorities);

        Mockito.when(authService.createUserDetails(loginRequest)).thenReturn(userDetails);
        Mockito.when(authService.createAccessTokenCookie(userDetails)).thenReturn(createCookie(ACCESS_TOKEN_COOKIE_NAME));
        Mockito.when(authService.createRefreshTokenCookie(userDetails)).thenReturn(createCookie(REFRESH_TOKEN_COOKIE_NAME));
        //when
        ResponseEntity<?> responseEntity = authController.authenticateUser(loginRequest);
        //then
        Mockito.verify(authService).createUserDetails(loginRequest);
        Mockito.verify(authService).createAccessTokenCookie(userDetails);
        Mockito.verify(authService).createRefreshTokenCookie(userDetails);

        UserInfoResponse userInfoResponse = (UserInfoResponse) responseEntity.getBody();
        assertThat(userInfoResponse).isNotNull();
        assertThat(userInfoResponse.getUsername()).isEqualTo(indexNumber);
        assertUserRoles(userInfoResponse, authorities);

        HttpHeaders headers = responseEntity.getHeaders();
        assertThatHeadersContainRequiredCookies(headers, List.of(ACCESS_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_NAME));

    }

    @Test
    void logoutUser_successful() {
        //given
        Mockito.when(jwtUtils.getCleanJwtCookie()).thenReturn(createCookie(ACCESS_TOKEN_COOKIE_NAME));
        Mockito.when(jwtUtils.getCleanJwtRefreshCookie()).thenReturn(createCookie(REFRESH_TOKEN_COOKIE_NAME));
        //when
        ResponseEntity<?> responseEntity = authController.logoutUser();
        //then
        Mockito.verify(jwtUtils).getCleanJwtCookie();
        Mockito.verify(jwtUtils).getCleanJwtRefreshCookie();

        HttpHeaders headers = responseEntity.getHeaders();
        assertThatHeadersContainRequiredCookies(headers, List.of(ACCESS_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_NAME));
    }

    private void assertThatHeadersContainRequiredCookies(HttpHeaders headers, List<String> cookiesNames) {
        List<String> cookiesFromHeader = headers.get(HttpHeaders.SET_COOKIE);
        assertThat(cookiesFromHeader).isNotEmpty();
        assertThat(cookiesFromHeader).hasSize(cookiesNames.size());
        cookiesNames.forEach(cookieName -> {
            boolean isCookieInHeader = cookiesFromHeader.stream()
                    .anyMatch(cookieFromHeader -> cookieFromHeader.contains(cookieName));
            assertTrue(isCookieInHeader);
        });
    }

    private void assertUserRoles(UserInfoResponse userInfoResponse, Set<GrantedAuthority> authorities) {
        List<String> authoritiesList = authorities.stream()
                .map(Object::toString)
                .toList();
        userInfoResponse.getRoles().forEach(role ->
                assertThat(authoritiesList).contains(role));
    }

    private Set<GrantedAuthority> createAuthorities(List<UserRole> userRoles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        userRoles.forEach(userRole -> authorities.add(new SimpleGrantedAuthority(userRole.name())));
        return authorities;
    }

}
