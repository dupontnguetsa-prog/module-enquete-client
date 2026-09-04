package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="api_keys")
public class ApiKey {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) private User owner;
    @Column(nullable=false,unique=true,length=128) private String keyHash;
    @Column(nullable=false,length=120) private String name;
    @Column(nullable=false) private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
    public Long getId(){return id;} public User getOwner(){return owner;} public void setOwner(User v){owner=v;}
    public String getKeyHash(){return keyHash;} public void setKeyHash(String v){keyHash=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getRevokedAt(){return revokedAt;} public void setRevokedAt(LocalDateTime v){revokedAt=v;}
    @PrePersist void prePersist(){if(createdAt==null)createdAt=LocalDateTime.now();}
}
