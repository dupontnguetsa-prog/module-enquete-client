package cm.afriland.enquete.repository;
import cm.afriland.enquete.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ApiKeyRepository extends JpaRepository<ApiKey,Long> {
    Optional<ApiKey> findByKeyHashAndRevokedAtIsNull(String keyHash);
    List<ApiKey> findAllByOwnerOrderByCreatedAtDesc(User owner);
}
