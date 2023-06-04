package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.entity.RefreshToken;
import pl.edu.amu.wmi.model.LoginRequest;
import pl.edu.amu.wmi.security.JwtUtils;
import pl.edu.amu.wmi.security.UserDetailsImpl;
import pl.edu.amu.wmi.service.AuthService;
import pl.edu.amu.wmi.service.RefreshTokenService;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public UserDetailsImpl createUserDetails(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    @Override
    public ResponseCookie createAccessTokenCookie(UserDetailsImpl userDetails) {
        return jwtUtils.generateJwtCookie(userDetails);
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(UserDetailsImpl userDetails) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
    }
}
