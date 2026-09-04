package cm.afriland.enquete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModuleEnqueteClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModuleEnqueteClientApplication.class, args);
    }
}
