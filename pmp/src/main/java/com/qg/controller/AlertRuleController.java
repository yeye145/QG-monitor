package com.qg.controller;

import com.qg.service.AlertRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertRules")
public class AlertRuleController {

    @Autowired
    private AlertRuleService alertRuleService;


}
