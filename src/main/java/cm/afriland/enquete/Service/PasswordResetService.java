package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.PasswordResetToken;
import cm.afriland.enquete.model.User;
import cm.afriland.enquete.repository.PasswordResetTokenRepository;
import cm.afriland.enquete.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final SurveyMailService mailService;

    public PasswordResetService(
        UserRepository users,
        PasswordResetTokenRepository tokens,
        PasswordEncoder encoder,
        SurveyMailService mailService
    ) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.mailService = mailService;
    }

    @Transactional
    public void requestReset(String email, String frontendUrl) {
        Optional<User> found = users.findByEmail(email.toLowerCase());
        if (found.isEmpty() || found.get().getEmail() == null || found.get().getEmail().isBlank()) return;

        User user = found.get();
        tokens.deleteActiveTokensForUser(user);
        byte[] value = new byte[32];
        secureRandom.nextBytes(value);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(value);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(TOKEN_TTL));
        tokens.save(token);
        mailService.sendPasswordReset(user.getEmail(), frontendUrl + "/reinitialiser-mot-de-passe?token=" + rawToken);
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) return false;
        Optional<PasswordResetToken> found = tokens.findByTokenHash(hash(rawToken.trim()));
        if (found.isEmpty()) return false;

        PasswordResetToken token = found.get();
        Instant now = Instant.now();
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) return false;

        token.setUsedAt(now);
        token.getUser().setPassword(encoder.encode(newPassword));
        tokens.save(token);
        return true;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte current : digest) result.append(String.format("%02x", current));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible.", exception);
        }
    }
}
