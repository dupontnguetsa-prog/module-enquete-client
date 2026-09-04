package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.*;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkspaceController {
    private final WorkspaceService service; private final UserService users;
    public WorkspaceController(WorkspaceService service, UserService users) { this.service = service; this.users = users; }
    private User current(HttpServletRequest request) { User user = AuthContext.currentUser(request, users); if (user == null) throw new SurveyController.UnauthorizedException(); return user; }
    @GetMapping("/teams") public Object teams(HttpServletRequest r) { return service.teams(current(r).getId()); }
    @PostMapping("/teams") public Object createTeam(@RequestBody NameRequest body, HttpServletRequest r) { return service.createTeam(current(r).getId(), body == null ? null : body.name()); }
    @GetMapping("/teams/{id}") public Object team(@PathVariable Long id, HttpServletRequest r) { return service.team(current(r).getId(), id); }
    @GetMapping("/teams/{id}/members") public Object teamMembers(@PathVariable Long id, HttpServletRequest r) { return service.teamMembers(current(r).getId(), id); }
    @GetMapping("/workspaces") public Object workspaces(HttpServletRequest r) { return service.workspaces(current(r).getId()); }
    @PostMapping("/workspaces") public Object createWorkspace(@RequestBody WorkspaceRequest body, HttpServletRequest r) { return service.createWorkspace(current(r).getId(), body.teamId(), body.name()); }
    @GetMapping("/workspaces/{id}") public Object workspace(@PathVariable Long id, HttpServletRequest r) { return service.workspace(current(r).getId(), id); }
    @GetMapping("/workspaces/{id}/members") public Object workspaceMembers(@PathVariable Long id, HttpServletRequest r) { return service.workspaceMembers(current(r).getId(), id); }
    @PostMapping("/teams/{id}/members") public Object teamMember(@PathVariable Long id, @RequestBody MemberRequest body, HttpServletRequest r) { return service.addTeamMember(current(r).getId(), id, body.userId(), body.role()); }
    @PostMapping("/workspaces/{id}/members") public Object workspaceMember(@PathVariable Long id, @RequestBody MemberRequest body, HttpServletRequest r) { return service.addWorkspaceMember(current(r).getId(), id, body.userId(), body.role()); }
    public record NameRequest(String name) {}
    public record WorkspaceRequest(String name, Long teamId) {}
    public record MemberRequest(Long userId, String role) {}
}
