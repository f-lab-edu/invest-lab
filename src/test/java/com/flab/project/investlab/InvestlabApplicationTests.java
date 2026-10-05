package com.flab.project.investlab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InvestlabApplicationTests {

    @Test
    void contextLoads() {
    }

}
