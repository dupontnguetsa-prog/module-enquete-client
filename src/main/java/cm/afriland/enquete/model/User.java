package cm.afriland.enquete.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nom;

    @Column(unique = true, length = 180)
    private String email;

    @Column(unique = true, nullable = false, length = 80)
    private String identifiant;

    @Column(nullable = false, length = 120)
    private String password;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(length = 32)
    private String role = "USER";

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getRole() { return role == null || role.isBlank() ? "USER" : role; }
    public void setRole(String role) { this.role = role; }
}
