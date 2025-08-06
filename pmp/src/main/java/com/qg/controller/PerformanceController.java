package com.qg.controller;

import com.qg.domain.Performance;
import com.qg.domain.Result;
import com.qg.service.PerformanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name ="性能检测")
@RestController
@RequestMapping("/performances")
public class PerformanceController {
    @Autowired
    private PerformanceService performanceService;

    /**
     * 插入性能数据
     * @param performance
     * @return
     */
    @PostMapping
    public Result addPerformance(@RequestBody List< Performance>  performance){
        return performanceService.addPerformance(performance);
    }

    /**
     * 查询该项目的性能数据
     * @param projectId
     * @return
     */
    @GetMapping
    public Result selectByProjectId(@RequestParam String projectId){
        return performanceService.selectByProjectId(projectId);
    }

    /**
     * 查询该项目的指定环境下的性能数据
     * @param env
     * @param projectId
     * @return
     */
    @GetMapping("/selectByEnvProjectId")
    public Result selectByEnvProjectId(@RequestParam String env, @RequestParam String projectId){
        return performanceService.selectByEnvProjectId(env,projectId);
    }
}
