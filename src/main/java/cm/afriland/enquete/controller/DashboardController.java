package cm.afriland.enquete.controller;

import cm.afriland.enquete.Service.UserService;
import cm.afriland.enquete.repository.SurveyRepository;
import cm.afriland.enquete.repository.SurveyResponseRepository;
import cm.afriland.enquete.repository.SurveyDeliveryEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final UserService users;private final SurveyRepository surveys;
    public DashboardController(UserService users,SurveyRepository surveys){this.users=users;this.surveys=surveys;}
    @GetMapping public Object dashboard(HttpServletRequest r){var u=AuthContext.currentUser(r,users);if(u==null)throw new SurveyController.UnauthorizedException();var list=surveys.findAllByOwnerOrderByUpdatedAtDesc(u);long active=list.stream().filter(s->"Active".equals(s.getStatus())).count();return Map.of("surveys",list.size(),"active",active,"recent",list.stream().limit(5).map(s->Map.of("id",s.getId(),"title",s.getTitle(),"status",s.getStatus(),"updatedAt",s.getUpdatedAt())).toList());}
}
