package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.AudienceService;
import cm.afriland.enquete.Service.UserService;
import cm.afriland.enquete.repository.CustomerProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/audiences")
public class AudienceController {
    private final AudienceService audience; private final UserService users; private final CustomerProfileRepository customers;
    public AudienceController(AudienceService audience,UserService users,CustomerProfileRepository customers){this.audience=audience;this.users=users;this.customers=customers;}

    @GetMapping("/options") public ResponseEntity<?> options(HttpServletRequest request){
        if(AuthContext.currentUser(request,users)==null)return ResponseEntity.status(401).build();
        var profiles=customers.findTop500ByOrderByIdAsc();
        java.util.function.Function<java.util.function.Function<cm.afriland.enquete.model.CustomerProfile,String>,java.util.List<String>> values=fn->profiles.stream().map(fn).filter(v->v!=null&&!v.isBlank()).distinct().sorted().toList();
        return ResponseEntity.ok(Map.of(
            "customerType", values.apply(cm.afriland.enquete.model.CustomerProfile::getCustomerType),
            "agency", values.apply(cm.afriland.enquete.model.CustomerProfile::getAgency),
            "city", values.apply(cm.afriland.enquete.model.CustomerProfile::getCity),
            "relationshipStatus", values.apply(cm.afriland.enquete.model.CustomerProfile::getRelationshipStatus),
            "product", values.apply(cm.afriland.enquete.model.CustomerProfile::getProduct)
        ));
    }
    @PostMapping("/preview") public ResponseEntity<?> preview(@RequestBody(required=false) Map<String,Object> body,HttpServletRequest request){if(AuthContext.currentUser(request,users)==null)return ResponseEntity.status(401).build();List<Map<String,Object>> filters=body==null?List.of():(List<Map<String,Object>>)body.getOrDefault("filters",List.of());return ResponseEntity.ok(audience.preview(filters));}
}
