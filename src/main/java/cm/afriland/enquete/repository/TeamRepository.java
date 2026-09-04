package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.Team;
import cm.afriland.enquete.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByOwnerOrderByCreatedAtDesc(User owner);
}
