package cm.afriland.enquete.controller;
import cm.afriland.enquete.Service.*;
import cm.afriland.enquete.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/api-keys")
public class ApiKeyController {
    private final ApiKeyService service; private final UserService users;
    public ApiKeyController(ApiKeyService service,UserService users){this.service=service;this.users=users;}
    private User current(HttpServletRequest r){User u=AuthContext.currentUser(r,users);if(u==null)throw new SurveyController.UnauthorizedException();return u;}
    @GetMapping public Object list(HttpServletRequest r){return service.list(current(r));}
    @PostMapping public Object create(@RequestBody Map<String,String> body,HttpServletRequest r){return service.create(current(r),body==null?null:body.get("name"));}
    @DeleteMapping("/{id}") public void revoke(@PathVariable Long id,HttpServletRequest r){service.revoke(current(r),id);}
}
