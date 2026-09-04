package cm.afriland.enquete.controller;
import cm.afriland.enquete.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/environment")
public class EnvironmentController {
    private final UserService users; private final String environment;
    public EnvironmentController(UserService users,@Value("${app.environment:development}") String environment){this.users=users;this.environment=environment;}
    @GetMapping public Map<String,String> current(HttpServletRequest request){if(AuthContext.currentUser(request,users)==null)throw new SurveyController.UnauthorizedException();return Map.of("name",environment,"mode","test".equalsIgnoreCase(environment)?"TEST":"LIVE");}
}
