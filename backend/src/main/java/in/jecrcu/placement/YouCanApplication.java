package in.jecrcu.placement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YouCanApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouCanApplication.class, args);
    }
}
