package com.project.articket.deactive;

import com.project.articket.deactive.service.DeactiveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeactiveServiceTests {

    @Autowired
    private DeactiveService deactiveService;

    @Test
    void releaseExpiredDeactivationsTest() {

        deactiveService.releaseExpiredDeactivations();
    }
}