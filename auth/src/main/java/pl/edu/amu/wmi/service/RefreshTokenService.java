package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    public Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    int deleteByUserId(Long userId);

}
