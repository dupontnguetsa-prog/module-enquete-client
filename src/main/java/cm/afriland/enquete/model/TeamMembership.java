package cm.afriland.enquete.model;

import jakarta.persistence.*;

@Entity
@Table(name = "team_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
public class TeamMembership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "team_id") private Team team;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, length = 20) private String role;
    public Long getId() { return id; }
    public Team getTeam() { return team; } public void setTeam(Team v) { team = v; }
    public User getUser() { return user; } public void setUser(User v) { user = v; }
    public String getRole() { return role; } public void setRole(String v) { role = v; }
}
