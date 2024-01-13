package pl.edu.amu.wmi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.entity.RefreshToken;
import pl.edu.amu.wmi.exception.TokenRefreshException;
import pl.edu.amu.wmi.model.LoginRequest;
import pl.edu.amu.wmi.model.UserInfoResponse;
import pl.edu.amu.wmi.security.JwtUtils;
import pl.edu.amu.wmi.security.UserDetailsImpl;
import pl.edu.amu.wmi.service.AuthService;
import pl.edu.amu.wmi.service.RefreshTokenService;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    private final JwtUtils jwtUtils;

    private final AuthService authService;

    public AuthController(RefreshTokenService refreshTokenService, JwtUtils jwtUtils, AuthService authService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserDetailsImpl userDetails = authService.createUserDetails(loginRequest);

        ResponseCookie jwtCookie = authService.createAccessTokenCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        ResponseCookie jwtRefreshCookie = authService.createRefreshTokenCookie(userDetails);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new UserInfoResponse(userDetails.getId().toString(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);

        if ((refreshToken != null) && (refreshToken.length() > 0)) {
            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .build();
                    })
                    .orElseThrow(() -> new TokenRefreshException(refreshToken,
                            "Refresh token is not in database!"));
        }

        return ResponseEntity.badRequest().body("Refresh Token is empty!");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {

        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}
