package com.qg.controller;

import com.qg.domain.Error;
import com.qg.domain.Result;
import com.qg.service.AlertRuleService;
import com.qg.service.ErrorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "错误信息")
@RestController
@RequestMapping("/errors")
public class ErrorController {

    @Autowired
    private ErrorService errorService;

    @PostMapping("/addError")
    public Result addError(@RequestBody List<Error> errorList) {
        return errorService.addError(errorList);
    }
}
