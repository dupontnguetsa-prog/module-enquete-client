package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findAllByOwnerOrderByCreatedAtDesc(User owner);
    List<Workspace> findAllByTeamOrderByCreatedAtDesc(Team team);
}
