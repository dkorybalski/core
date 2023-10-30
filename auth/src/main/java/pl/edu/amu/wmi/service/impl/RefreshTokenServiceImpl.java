package pl.edu.amu.wmi.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.RefreshTokenDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.RefreshToken;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.exception.TokenRefreshException;
import pl.edu.amu.wmi.service.RefreshTokenService;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${pri.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenDAO refreshTokenDAO;

    private final UserDataDAO userDataDAO;

    public RefreshTokenServiceImpl(RefreshTokenDAO refreshTokenDAO, UserDataDAO userDataDAO) {
        this.refreshTokenDAO = refreshTokenDAO;
        this.userDataDAO = userDataDAO;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenDAO.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        UserData user = userDataDAO.findById(userId).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with id: {0} not found", userId)));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenDAO.save(refreshToken);
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenDAO.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        UserData user = userDataDAO.findById(userId).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with id: {0} not found", userId)));
        return refreshTokenDAO.deleteByUser(user);
    }
}
