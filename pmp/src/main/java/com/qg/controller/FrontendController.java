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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/{type}")
    public void getData(@RequestBody String data,@PathVariable  String type) {
        log.info("***********接收到了前端数据***********");
        log.info(data);
        switch (type) {
            case "performance":
                List<FrontendPerformance> performanceList = JSONUtil.toList(data, FrontendPerformance.class);
                frontendPerformanceService.saveFrontendPerformance(performanceList);
                log.info("已接收的前端性能数据: " + performanceList);
                break;
            case "error":
                List<FrontendError> errorList = JSONUtil.toList(data, FrontendError.class);
                frontendErrorService.saveFrontendError(errorList);
                log.info("已接收的前端错误数据: " + errorList);
                break;
            case "behavior":
                List<FrontendBehavior> behaviorList = JSONUtil.toList(data, FrontendBehavior.class);
                frontendBehaviorService.saveFrontendBehavior(behaviorList);
                log.info("已接收的前端行为数据: " + behaviorList);
                break;
            default:
                log.error("未知的数据类型: " + type);
        }

    }

    @PostMapping("/formData")
    public void getFile(@RequestParam String projectId, @RequestParam String timestamp, @RequestParam String version,
                        @RequestParam String buildVersion, @RequestParam MultipartFile[] files,
                        @RequestParam String [] jsFilenames, @RequestParam String fileHashes) {
        log.info("接收到前端上传的文件数据");
    }






}
