package cm.afriland.enquete.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/api/health") public Map<String,String> health(){try{jdbc.queryForObject("select 1",Integer.class);return Map.of("application","UP","database","UP");}catch(Exception e){return Map.of("application","UP","database","DOWN");}}
}
