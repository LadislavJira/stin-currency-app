package cz.tul.stin.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Opět podstrčíme proměnné
@SpringBootTest(properties = {
        "API_USERNAME=testAdmin",
        "API_PASSWORD=testPassword123"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}