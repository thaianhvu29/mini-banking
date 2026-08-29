package com.minibanking.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=mini-banking-test-secret-key-12345678901234567890",
        "app.auth.username=test-user",
        "app.auth.password=test-pass"
})
class GatewayServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
