package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspaces")
public class Workspace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160) private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false) private Team team;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false) private User owner;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public Team getTeam() { return team; } public void setTeam(Team v) { team = v; }
    public User getOwner() { return owner; } public void setOwner(User v) { owner = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
