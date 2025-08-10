package com.qg.controller;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qg.domain.*;
import com.qg.service.BackendErrorService;
import com.qg.service.BackendLogService;
import com.qg.service.BackendPerformanceService;
import com.qg.service.MethodInvocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private MethodInvocationService methodInvocationService;

    @PostMapping("/getMethodUseCount")
    public void getMethodUseCount(@RequestBody String encodedData) {
        try {
            // 解码和验证
            String decodedData = URLDecoder.decode(encodedData, StandardCharsets.UTF_8);
            if (!decodedData.contains("@")) {
                log.warn("传入的数据格式有误: {}", encodedData);
                return;
            }

            // 分割数据
            String[] parts = decodedData.split("@", 2);
            String projectId = parts[0].trim();
            String mapJSON = parts[1].trim();

            // 解析和验证
            if (StrUtil.isBlank(projectId) || StrUtil.isBlank(mapJSON)) {
                log.warn("传入的项目id或方法使用情况为空");
                return;
            }

            JSONObject jsonObj = JSONUtil.parseObj(mapJSON);
            Map<String, Integer> methodMap = jsonObj.toBean(new TypeReference<>() {});

            if (methodMap.isEmpty()) {
                log.warn("方法使用情况为空");
                return;
            }

            methodInvocationService.statisticsMethod(methodMap, projectId);

        } catch (Exception e) {
            log.error("统计方法过程出现异常: {}", e.getMessage());
        }
    }

    @PostMapping("/performance")
    public void getPerformanceData(@RequestBody String performanceData) {
        System.out.println("***********接收到了后端性能数据***********");
        backendPerformanceService.addPerformance(performanceData);
    }

    @PostMapping("/error")
    public void getErrorData(@RequestBody String errorData) {
        log.info("***********接收到了后端错误信息***********");
        log.info(errorData);
        backendErrorService.addBackendError(errorData);
    }

    /**
     * 接收后端SDK日志
     *
     * @param logJSON
     */
    @PostMapping("/log")
    public void receiveLogFromSDK(@RequestBody String logJSON) {
        backendLogService.receiveLogFromSDK(logJSON);
    }
}

