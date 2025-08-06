package com.qg.controller;

import com.qg.domain.Performance;
import com.qg.domain.Result;
import com.qg.service.PerformanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name ="性能检测")
@RestController
@RequestMapping("/performances")
public class PerformanceController {
    @Autowired
    private PerformanceService performanceService;

    @PostMapping
    public Result addPerformance(@RequestBody List< Performance>  performance){
        return performanceService.addPerformance(performance);
    }
}
