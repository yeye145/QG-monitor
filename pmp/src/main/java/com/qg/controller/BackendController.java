package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.BackendError;
import com.qg.domain.BackendLog;
import com.qg.domain.BackendPerformance;
import com.qg.domain.Result;
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
    public void getPerformanceData(@RequestBody String performanceData) {
        log.info("***********接收到了后端性能数据***********");
        log.info(performanceData);
        List<BackendPerformance> backendPerformances = JSONUtil.toList(performanceData, BackendPerformance.class);
        if (backendPerformanceService.saveBackendPerformance(backendPerformances) > 0) {
            log.info("已接收的后端性能数据: " + backendPerformances);
        }else {
            log.error("接收后端性能数据失败");
        }
    }

    @PostMapping("/error")
    public Result getErrorData(@RequestBody String errorData) {
        log.info("***********接收到了后端错误信息***********");
        log.info(errorData);
        return backendErrorService.addBackendError(errorData);
    }

    @PostMapping("/log")
    public void getLogData(@RequestBody String logData) {
        log.info("***********接收到了后端日志信息***********");
        log.info(logData);
        List<BackendLog> backendLogs = JSONUtil.toList(logData, BackendLog.class);
        if (backendLogService.saveBackendLogs(backendLogs) > 0) {
            log.info("已接收的后端日志信息: " + backendLogs);
        } else {
            log.error("接收后端日志信息失败");
        }
    }


}
