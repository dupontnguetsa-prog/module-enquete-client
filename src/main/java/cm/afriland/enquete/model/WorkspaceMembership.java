package cm.afriland.enquete.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workspace_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"}))
public class WorkspaceMembership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "workspace_id") private Workspace workspace;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, length = 20) private String role;
    public Long getId() { return id; }
    public Workspace getWorkspace() { return workspace; } public void setWorkspace(Workspace v) { workspace = v; }
    public User getUser() { return user; } public void setUser(User v) { user = v; }
    public String getRole() { return role; } public void setRole(String v) { role = v; }
}
