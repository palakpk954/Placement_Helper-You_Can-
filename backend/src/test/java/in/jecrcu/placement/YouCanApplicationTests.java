package in.jecrcu.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YouCanApplicationTests {

    static {
        System.setProperty("spring.classformat.ignore", "true");
    }

    @Test
    void contextLoads() {
        // Verifies Spring context, Flyway migrations (V1 & V2), JPA mapping, and beans load cleanly
    }
}

