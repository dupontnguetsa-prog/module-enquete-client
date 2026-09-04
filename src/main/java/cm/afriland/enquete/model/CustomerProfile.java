package cm.afriland.enquete.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 140) private String name;
    @Column(unique = true, length = 180) private String email;
    @Column(length = 60) private String customerType;
    @Column(length = 120) private String agency;
    @Column(length = 80) private String city;
    @Column(length = 60) private String relationshipStatus;
    @Column(length = 120) private String product;
    private Integer tenureMonths;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getCustomerType(){return customerType;} public void setCustomerType(String v){customerType=v;}
    public String getAgency(){return agency;} public void setAgency(String v){agency=v;} public String getCity(){return city;} public void setCity(String v){city=v;}
    public String getRelationshipStatus(){return relationshipStatus;} public void setRelationshipStatus(String v){relationshipStatus=v;} public String getProduct(){return product;} public void setProduct(String v){product=v;}
    public Integer getTenureMonths(){return tenureMonths;} public void setTenureMonths(Integer v){tenureMonths=v;}
}
