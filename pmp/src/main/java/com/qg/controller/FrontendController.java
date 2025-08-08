package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.FrontendBehavior;
import com.qg.domain.FrontendError;
import com.qg.domain.FrontendPerformance;
import com.qg.service.FrontendBehaviorService;
import com.qg.service.FrontendErrorService;
import com.qg.service.FrontendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 前端业务类  // 类说明
 * @ClassName: FrontendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:59   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@RequestMapping("/frontend")
@RestController
public class FrontendController {

    @Autowired
    private FrontendPerformanceService frontendPerformanceService;

    @Autowired
    private FrontendErrorService frontendErrorService;

    @Autowired
    private FrontendBehaviorService frontendBehaviorService;


    @PostMapping("/performance")
    public void getPerformanceData(@RequestBody String performanceData) {
        log.info("***********接收到了前端性能数据***********");
        log.info(performanceData);
        List<FrontendPerformance> frontendPerformance = JSONUtil.toList(performanceData, FrontendPerformance.class);
        if (frontendPerformanceService.saveFrontendPerformance(frontendPerformance) > 0) {
            log.info("已接收的前端性能数据: " + frontendPerformance);
        } else {
            log.error("接收前端性能数据失败");
        }
    }

    @PostMapping("/error")
    public void getErrorData(@RequestBody String errorData) {
        log.info("***********接收到了前端错误数据***********");
        log.info(errorData);
        List<FrontendError> frontendErrors = JSONUtil.toList(errorData, FrontendError.class);
        if (frontendErrorService.saveFrontendError(frontendErrors) > 0) {
            log.info("已接收的前端错误数据: " + frontendErrors);
        } else {
            log.error("接收前端错误数据失败");
        }
    }

    @PostMapping("/behavior")
    public void getBehaviorData(@RequestBody String behaviorData) {
        log.info("***********接收到了前端行为数据***********");
        log.info(behaviorData);
        List<FrontendBehavior> behaviorList = JSONUtil.toList(behaviorData, FrontendBehavior.class);
        if (frontendBehaviorService.saveFrontendBehavior(behaviorList) > 0) {
            log.info("已接收的前端行为数据: " + behaviorList);
        } else {
            log.error("接收前端行为数据失败");
        }
    }


}
