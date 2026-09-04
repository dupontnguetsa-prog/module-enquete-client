package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    Optional<TeamMembership> findByTeamAndUser(Team team, User user);
    List<TeamMembership> findAllByUser(User user);
    List<TeamMembership> findAllByTeam(Team team);
}
