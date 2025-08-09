package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.*;
import com.qg.service.BackendErrorService;
import com.qg.service.BackendLogService;
import com.qg.service.BackendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 后端业务类  // 类说明
 * @ClassName: BackendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 22:00   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@RequestMapping("/backend")
@RestController
public class BackendController {

    @Autowired
    private BackendPerformanceService backendPerformanceService;

    @Autowired
    private BackendErrorService backendErrorService;

    @Autowired
    private BackendLogService backendLogService;

    @PostMapping("/getMethodUseCount")
    public void getMethodUseCount(@RequestBody String methodCount) {
        log.info("***********接收到了方法调用情况信息***********");
        log.info(methodCount);
    }

    @PostMapping("/performance")
    public String getPerformanceData(@RequestBody String performanceData) {
        System.out.println("***********接收到了后端性能数据***********");
        List<BackendPerformance> backendPerformances = JSONUtil.toList(performanceData, BackendPerformance.class);
        backendPerformances.forEach(System.out::println);
        return "";
    }

    @PostMapping("/error")
    public Result getErrorData(@RequestBody String errorData) {
        log.info("***********接收到了后端错误信息***********");
        log.info(errorData);
        return backendErrorService.addBackendError(errorData);
    }

    @PostMapping("/log")
    public void receiveLogFromSDK(@RequestBody String logJSON) {
        log.info(backendLogService.receiveLogFromSDK(logJSON));
    }
}

