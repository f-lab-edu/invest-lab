package com.flab.project.investlab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InvestlabApplicationTests {

    @Test
    void 애플리케이션_컨텍스트를_불러온다() {
    }

}
