package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.*;
import com.qg.service.FrontendBehaviorService;
import com.qg.service.FrontendErrorService;
import com.qg.service.FrontendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.qg.domain.Code.INTERNAL_ERROR;

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


//    @PostMapping("/performance")
//    public void getPerformanceData(@RequestBody String performanceData) {
//        log.info("***********接收到了前端性能数据***********");
//        log.info(performanceData);
//        List<FrontendPerformance> frontendPerformance = JSONUtil.toList(performanceData, FrontendPerformance.class);
//        if (frontendPerformanceService.saveFrontendPerformance(frontendPerformance) > 0) {
//            log.info("已接收的前端性能数据: " + frontendPerformance);
//        } else {
//            log.error("接收前端性能数据失败");
//        }
//    }
//
//    @PostMapping("/error")
//    public Result getErrorData(@RequestBody String errorData) {
//        log.info("***********接收到了前端错误数据***********");
//        log.info(errorData);
//        return frontendErrorService.addFrontendError(errorData);
//    }
//
//    @PostMapping("/behavior")
//    public void getBehaviorData(@RequestBody String behaviorData) {
//        log.info("***********接收到了前端行为数据***********");
//        log.info(behaviorData);
//        List<FrontendBehavior> behaviorList = JSONUtil.toList(behaviorData, FrontendBehavior.class);
//        if (frontendBehaviorService.saveFrontendBehavior(behaviorList) > 0) {
//            log.info("已接收的前端行为数据: " + behaviorList);
//        } else {
//            log.error("接收前端行为数据失败");
//        }
//    }

    @PostMapping("/{type}")
    public Result getData(@RequestBody String data,@PathVariable String type) {

        switch (type) {
            case "performance":
                log.info("************接收到前端性能数据*************");
                log.info("前端性能数据: " + data);
                return frontendPerformanceService.saveFrontendPerformance(data);
            case "error":
                log.info("************接收到前端错误数据*************");
                log.info("前端错误数据: " + data);
                return frontendErrorService.addFrontendError(data);
            case "behavior":
                log.info("************接收到前端用户行为数据*************");
                log.info("前端用户行为数据: " + data);
                return frontendBehaviorService.saveFrontendBehavior(data);
            default:
                log.error("未知的数据类型: " + type);
                return new Result(INTERNAL_ERROR, "未知的数据类型");
        }

    }

    @PostMapping("/formData")
    public void getFile(@RequestParam String projectId, @RequestParam String timestamp, @RequestParam String version,
                        @RequestParam String buildVersion, @RequestParam MultipartFile[] files,
                        @RequestParam String [] jsFilenames, @RequestParam String fileHashes) {
        log.info("接收到前端上传的文件数据");
        log.info("项目ID: " + projectId,
                "时间戳: " + timestamp,
                "版本: " + version,
                "构建版本: " + buildVersion,
                "文件数量: " + files.length,
                "JS文件名: " + String.join(", ", jsFilenames),
                "文件哈希: " + fileHashes);
    }






}
