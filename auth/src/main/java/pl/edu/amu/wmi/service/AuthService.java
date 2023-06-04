package pl.edu.amu.wmi.service;

import org.springframework.http.ResponseCookie;
import pl.edu.amu.wmi.model.LoginRequest;
import pl.edu.amu.wmi.security.UserDetailsImpl;

public interface AuthService {

    UserDetailsImpl createUserDetails(LoginRequest loginRequest);

    ResponseCookie createAccessTokenCookie(UserDetailsImpl userDetails);

    ResponseCookie createRefreshTokenCookie(UserDetailsImpl userDetails);
}
