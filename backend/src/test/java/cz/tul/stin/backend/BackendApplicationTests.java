package cz.tul.stin.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "API_USERNAME=testAdmin",
        "API_PASSWORD=testPassword123",
        "EXCHANGE_API_KEY=fakeTestKey"
})
@ActiveProfiles("prod")
class BackendApplicationTests {

    @Test
    void contextLoads() {

    }

    @Test
    void mainMethodTest() {
        BackendApplication.main(new String[]{
                "--server.port=0",
                "--EXCHANGE_API_KEY=fakeTestKey",
                "--API_USERNAME=testAdmin",
                "--API_PASSWORD=testPassword123"
        });
    }

}