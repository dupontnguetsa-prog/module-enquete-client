package cm.afriland.enquete.Service;

import cm.afriland.enquete.model.*;
import cm.afriland.enquete.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class WorkspaceService {
    private static final Set<String> ROLES = Set.of("OWNER", "ADMIN", "EDITOR", "ANALYST", "SUPPORT");
    private final UserRepository users;
    private final TeamRepository teams;
    private final WorkspaceRepository workspaces;
    private final TeamMembershipRepository teamMembers;
    private final WorkspaceMembershipRepository workspaceMembers;

    public WorkspaceService(UserRepository users, TeamRepository teams, WorkspaceRepository workspaces,
                            TeamMembershipRepository teamMembers, WorkspaceMembershipRepository workspaceMembers) {
        this.users = users; this.teams = teams; this.workspaces = workspaces;
        this.teamMembers = teamMembers; this.workspaceMembers = workspaceMembers;
    }

    @Transactional(readOnly = true)
    public List<TeamView> teams(Long userId) {
        User user = user(userId);
        return teamMembers.findAllByUser(user).stream().map(TeamMembership::getTeam).distinct()
            .map(this::teamView).toList();
    }

    @Transactional
    public TeamView createTeam(Long userId, String name) {
        String value = required(name, "Le nom de l'équipe est obligatoire.");
        User owner = user(userId);
        Team team = new Team(); team.setName(value); team.setSlug(slug(value)); team.setOwner(owner);
        team = teams.save(team);
        member(team, owner, "OWNER");
        return teamView(team);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceView> workspaces(Long userId) {
        User user = user(userId);
        return workspaceMembers.findAllByUser(user).stream().map(WorkspaceMembership::getWorkspace).distinct()
            .map(this::workspaceView).toList();
    }

    @Transactional
    public WorkspaceView createWorkspace(Long userId, Long teamId, String name) {
        User owner = user(userId); Team team = requireTeam(teamId);
        requireTeamRole(team, owner, "OWNER", "ADMIN");
        Workspace workspace = new Workspace(); workspace.setName(required(name, "Le nom de l'espace est obligatoire."));
        workspace.setTeam(team); workspace.setOwner(owner); workspace = workspaces.save(workspace);
        member(workspace, owner, "OWNER");
        return workspaceView(workspace);
    }

    @Transactional
    public MemberView addTeamMember(Long userId, Long teamId, Long memberId, String role) {
        User actor = user(userId); Team team = requireTeam(teamId); requireTeamRole(team, actor, "OWNER", "ADMIN");
        return teamMember(team, users.findById(memberId).orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable.")), role);
    }

    @Transactional
    public MemberView addWorkspaceMember(Long userId, Long workspaceId, Long memberId, String role) {
        User actor = user(userId); Workspace workspace = requireWorkspace(workspaceId);
        requireWorkspaceRole(workspace, actor, "OWNER", "ADMIN");
        return workspaceMember(workspace, users.findById(memberId).orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable.")), role);
    }
    @Transactional(readOnly = true)
    public TeamView team(Long userId, Long teamId) { Team team = requireAccessibleTeam(userId, teamId); return teamView(team); }
    @Transactional(readOnly = true)
    public WorkspaceView workspace(Long userId, Long workspaceId) { return workspaceView(requireAccessibleWorkspace(userId, workspaceId)); }
    @Transactional(readOnly = true)
    public List<MemberView> teamMembers(Long userId, Long teamId) {
        Team team = requireAccessibleTeam(userId, teamId);
        return teamMembers.findAllByTeam(team).stream().map(m -> new MemberView(m.getUser().getId(), m.getUser().getNom(), m.getUser().getEmail(), m.getRole())).toList();
    }
    @Transactional(readOnly = true)
    public List<MemberView> workspaceMembers(Long userId, Long workspaceId) {
        Workspace workspace = requireAccessibleWorkspace(userId, workspaceId);
        return workspaceMembers.findAllByWorkspace(workspace).stream().map(m -> new MemberView(m.getUser().getId(), m.getUser().getNom(), m.getUser().getEmail(), m.getRole())).toList();
    }

    @Transactional(readOnly = true)
    public Workspace requireAccessibleWorkspace(Long userId, Long workspaceId) {
        Workspace workspace = requireWorkspace(workspaceId); User actor = user(userId);
        if (workspace.getOwner().getId().equals(actor.getId()) || workspaceMembers.findByWorkspaceAndUser(workspace, actor).isPresent()
            || teamMembers.findByTeamAndUser(workspace.getTeam(), actor).isPresent()) return workspace;
        throw new NoSuchElementException("Espace introuvable.");
    }

    @Transactional(readOnly = true)
    public Team requireAccessibleTeam(Long userId, Long teamId) {
        User actor = user(userId); Team team = requireTeam(teamId);
        if (team.getOwner().getId().equals(actor.getId()) || teamMembers.findByTeamAndUser(team, actor).isPresent()) return team;
        throw new NoSuchElementException("Equipe introuvable.");
    }

    public String roleFor(Workspace workspace, User user) {
        if (workspace.getOwner().getId().equals(user.getId())) return "OWNER";
        return workspaceMembers.findByWorkspaceAndUser(workspace, user).map(WorkspaceMembership::getRole)
            .orElseGet(() -> teamMembers.findByTeamAndUser(workspace.getTeam(), user).map(TeamMembership::getRole).orElse(null));
    }
    public String roleForTeam(Team team, User user) {
        if (team.getOwner().getId().equals(user.getId())) return "OWNER";
        return teamMembers.findByTeamAndUser(team, user).map(TeamMembership::getRole).orElse(null);
    }

    private void requireTeamRole(Team team, User user, String... allowed) {
        String role = team.getOwner().getId().equals(user.getId()) ? "OWNER" : teamMembers.findByTeamAndUser(team, user).map(TeamMembership::getRole).orElse(null);
        if (role == null || Arrays.stream(allowed).noneMatch(role::equals)) throw new IllegalStateException("Permission insuffisante.");
    }
    private void requireWorkspaceRole(Workspace workspace, User user, String... allowed) {
        String role = roleFor(workspace, user);
        if (role == null || Arrays.stream(allowed).noneMatch(role::equals)) throw new IllegalStateException("Permission insuffisante.");
    }
    private Team requireTeam(Long id) { return teams.findById(id).orElseThrow(() -> new NoSuchElementException("Equipe introuvable.")); }
    private Workspace requireWorkspace(Long id) { return workspaces.findById(id).orElseThrow(() -> new NoSuchElementException("Espace introuvable.")); }
    private User user(Long id) { return users.findById(id).orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable.")); }
    private void member(Team team, User user, String role) { TeamMembership m = new TeamMembership(); m.setTeam(team); m.setUser(user); m.setRole(role); teamMembers.save(m); }
    private void member(Workspace workspace, User user, String role) { workspaceMember(workspace, user, role); }
    private MemberView teamMember(Team team, User user, String role) {
        String normalized = validRole(role); TeamMembership m = teamMembers.findByTeamAndUser(team, user).orElseGet(TeamMembership::new);
        m.setTeam(team); m.setUser(user); m.setRole(normalized); m = teamMembers.save(m); return new MemberView(user.getId(), user.getNom(), user.getEmail(), m.getRole());
    }
    private MemberView workspaceMember(Workspace workspace, User user, String role) {
        String normalized = validRole(role); WorkspaceMembership m = workspaceMembers.findByWorkspaceAndUser(workspace, user).orElseGet(WorkspaceMembership::new);
        m.setWorkspace(workspace); m.setUser(user); m.setRole(normalized); m = workspaceMembers.save(m); return new MemberView(user.getId(), user.getNom(), user.getEmail(), m.getRole());
    }
    private String validRole(String role) { String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT); if (!ROLES.contains(normalized)) throw new IllegalArgumentException("Role invalide."); return normalized; }
    private String required(String value, String message) { if (value == null || value.isBlank()) throw new IllegalArgumentException(message); return value.trim(); }
    private String slug(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); }
    private TeamView teamView(Team t) { return new TeamView(t.getId(), t.getName(), t.getSlug(), t.getOwner().getId(), t.getCreatedAt()); }
    private WorkspaceView workspaceView(Workspace w) { return new WorkspaceView(w.getId(), w.getName(), w.getTeam().getId(), w.getOwner().getId(), w.getCreatedAt()); }
    public record TeamView(Long id, String name, String slug, Long ownerId, java.time.LocalDateTime createdAt) {}
    public record WorkspaceView(Long id, String name, Long teamId, Long ownerId, java.time.LocalDateTime createdAt) {}
    public record MemberView(Long userId, String name, String email, String role) {}
}
