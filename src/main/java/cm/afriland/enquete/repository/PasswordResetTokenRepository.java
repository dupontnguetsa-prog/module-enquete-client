package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.PasswordResetToken;
import cm.afriland.enquete.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from PasswordResetToken t where t.user = :user and t.usedAt is null")
    int deleteActiveTokensForUser(@Param("user") User user);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :now or t.usedAt is not null")
    int deleteExpiredOrUsed(@Param("now") Instant now);
}
