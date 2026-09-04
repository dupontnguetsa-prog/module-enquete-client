package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    List<CustomerProfile> findTop500ByOrderByIdAsc();
    java.util.Optional<CustomerProfile> findByEmailIgnoreCase(String email);
}
