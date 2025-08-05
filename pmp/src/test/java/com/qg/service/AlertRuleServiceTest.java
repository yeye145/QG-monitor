package com.qg.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AlertRuleServiceTest {

    @Autowired
    private AlertRuleService alertRuleService;

    @Test
    public void testSelectByType() {
        System.out.println(alertRuleService.selectByType("NullPointException"));
    }
}
