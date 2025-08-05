package com.qg.controller;

import com.qg.service.AlertRuleService;
import com.qg.service.ErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/errors")
public class ErrorController {

    @Autowired
    private ErrorService errorService;
}
