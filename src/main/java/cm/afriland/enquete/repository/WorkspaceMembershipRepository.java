package cm.afriland.enquete.repository;

import cm.afriland.enquete.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, Long> {
    Optional<WorkspaceMembership> findByWorkspaceAndUser(Workspace workspace, User user);
    List<WorkspaceMembership> findAllByWorkspace(Workspace workspace);
    List<WorkspaceMembership> findAllByUser(User user);
}
