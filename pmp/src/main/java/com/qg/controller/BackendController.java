package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.*;
import com.qg.service.BackendErrorService;
import com.qg.service.BackendLogService;
import com.qg.service.BackendPerformanceService;
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
    public String getMethodUseCount(@RequestBody String methodCount) {
        System.err.println("***********接收到了方法调用情况信息***********");
        System.err.println(methodCount);
        return JSONUtil.toJsonStr(new Result(200, "已接收方法调用情况信息"));
    }

    @PostMapping("/performance")
    public String getPerformanceData(@RequestBody String performanceData) {
        System.out.println("***********接收到了后端性能数据***********");
        List<BackendPerformance> backendPerformances = JSONUtil.toList(performanceData, BackendPerformance.class);
        backendPerformances.forEach(System.out::println);
        return "";
    }

    @PostMapping("/error")
    public String getErrorData(@RequestBody String errorData) {
        System.out.println("***********接收到了后端错误信息***********");
        System.out.println(errorData);
        BackendError backendError = JSONUtil.toBean(errorData, BackendError.class);
        if (backendError != null) {
            System.out.println("已接收的后端错误信息: " + backendError);
        }

        return JSONUtil.toJsonStr(new Result(200, "已接收错误信息"));
    }

    @PostMapping("/log")
    public String getLog(@RequestBody String logJSON) {
        System.err.println("=============接收到了日志信息***********");
        return backendLogService.getLog(logJSON);
    }

}
