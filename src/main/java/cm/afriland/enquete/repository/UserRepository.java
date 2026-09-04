package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentifiant(String identifiant);
    Optional<User> findByEmail(String email);
    boolean existsByIdentifiant(String identifiant);
    boolean existsByEmail(String email);
}
