package com.qg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.qg.domain.FrontendBehavior;
import com.qg.domain.FrontendError;
import com.qg.domain.FrontendPerformance;
import com.qg.mapper.FrontendBehaviorMapper;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.FrontendPerformanceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: 前端数据插入和查询测试
 * @ClassName: FrontendDataTest
 * @Author: lrt
 * @Date: 2025/8/11 17:30
 * @Version: 1.0
 */
@SpringBootTest
public class FrontendDataTest {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;

//    @Test
//    public void testFrontendErrorSaveAndRead() {
//        System.out.println("=== 测试 FrontendError 插入和查询 ===");
//
//        // 构造测试数据
//        FrontendError error = new FrontendError();
//        error.setProjectId("1");
//        error.setTimestamp(LocalDateTime.now());
//        error.setSessionId("session-123");
//        error.setUserAgent("Mozilla/5.0 Chrome/91.0");
//        error.setErrorType("ReferenceError");
//        error.setMessage("Cannot read property of undefined");
//        error.setStack("at Component.render (app.js:123)");
//        error.setEvent(1);
//        error.setCaptureType("automatic");
//        error.setDuration(1500L);
//
//        // 设置复杂对象
//        Map<String, Object> requestInfo = new HashMap<>();
//        requestInfo.put("url", "https://example.com/api/users");
//        requestInfo.put("method", "GET");
//        requestInfo.put("headers", Map.of("Authorization", "Bearer token123"));
//        error.setRequestInfo(requestInfo);
//
//        Map<String, Object> responseInfo = new HashMap<>();
//        responseInfo.put("status", 500);
//        responseInfo.put("statusText", "Internal Server Error");
//        error.setResponseInfo(responseInfo);
//
//        List<Map<String, Object>> breadcrumbs = new ArrayList<>();
//        Map<String, Object> breadcrumb1 = new HashMap<>();
//        breadcrumb1.put("timestamp", "2025-08-11T17:30:00");
//        breadcrumb1.put("category", "navigation");
//        breadcrumb1.put("message", "User navigated to /dashboard");
//        breadcrumbs.add(breadcrumb1);
//        error.setBreadcrumbs(breadcrumbs);
//
//        Map<String, Object> tags = new HashMap<>();
//        tags.put("browser", "Chrome");
//        tags.put("version", "91.0");
//        error.setTags(tags);
//
//        // 保存
//        frontendErrorMapper.insert(error);
//        System.out.println("保存的FrontendError ID: " + error.getId());
//
//        // 查询
//        FrontendError saved = frontendErrorMapper.selectById(error.getId());
//        System.out.println("请求信息: " + saved.getRequestInfo());
//        System.out.println("面包屑: " + saved.getBreadcrumbs());
//        System.out.println("标签: " + saved.getTags());
//    }

//
//    @Test
//    public void testFrontendPerformanceSaveAndRead() {
//        System.out.println("=== 测试 FrontendPerformance 插入和查询 ===");
//
//        // 构造测试数据
//        FrontendPerformance performance = new FrontendPerformance();
//        performance.setProjectId("1");
//        performance.setTimestamp(LocalDateTime.now());
//        performance.setSessionId("session-456");
//        performance.setUserAgent("Mozilla/5.0 Safari/14.0");
//        performance.setCaptureType("navigation");
//
//        // 设置性能指标
//        Map<String, Object> metrics = new HashMap<>();
//        metrics.put("loadTime", 2300);
//        metrics.put("firstContentfulPaint", 1200);
//        metrics.put("largestContentfulPaint", 2100);
//        metrics.put("timeToInteractive", 3500);
//        metrics.put("memoryUsage", Map.of("usedJSHeapSize", 15728640, "totalJSHeapSize", 31457280));
//        performance.setMetrics(metrics);
//
//        // 保存
//        frontendPerformanceMapper.insert(performance);
//        System.out.println("保存的FrontendPerformance ID: " + performance.getId());
//
//        // 查询
//        FrontendPerformance saved = frontendPerformanceMapper.selectById(performance.getId());
//        System.out.println("性能指标: " + saved.getMetrics());
//    }
//
//    @Test
//    public void testFrontendBehaviorSaveAndRead() {
//        System.out.println("=== 测试 FrontendBehavior 插入和查询 ===");
//
//        // 构造测试数据
//        FrontendBehavior behavior = new FrontendBehavior();
//        behavior.setProjectId("1");
//        behavior.setTimestamp(LocalDateTime.now());
//        behavior.setSessionId("session-789");
//        behavior.setUserAgent("Mozilla/5.0 Firefox/89.0");
//        behavior.setCaptureType("user-action");
//
//        // 设置页面信息
//        Map<String, Object> pageInfo = new HashMap<>();
//        pageInfo.put("url", "https://example.com/dashboard");
//        pageInfo.put("title", "Dashboard - Example App");
//        pageInfo.put("referrer", "https://example.com/login");
//        pageInfo.put("viewportSize", Map.of("width", 1920, "height", 1080));
//        behavior.setPageInfo(pageInfo);
//
//        // 设置用户行为轨迹
//        List<Map<String, Object>> breadcrumbs = new ArrayList<>();
//
//        Map<String, Object> action1 = new HashMap<>();
//        action1.put("timestamp", "2025-08-11T17:25:00");
//        action1.put("type", "click");
//        action1.put("element", "#login-button");
//        action1.put("coordinates", Map.of("x", 150, "y", 45));
//        breadcrumbs.add(action1);
//
//        Map<String, Object> action2 = new HashMap<>();
//        action2.put("timestamp", "2025-08-11T17:25:30");
//        action2.put("type", "navigation");
//        action2.put("from", "/login");
//        action2.put("to", "/dashboard");
//        breadcrumbs.add(action2);
//
//        Map<String, Object> action3 = new HashMap<>();
//        action3.put("timestamp", "2025-08-11T17:26:00");
//        action3.put("type", "scroll");
//        action3.put("scrollTop", 500);
//        breadcrumbs.add(action3);
//
//        behavior.setBreadcrumbs(breadcrumbs);
//
//        // 保存
//        frontendBehaviorMapper.insert(behavior);
//        System.out.println("保存的FrontendBehavior ID: " + behavior.getId());
//
//        // 查询
//        FrontendBehavior saved = frontendBehaviorMapper.selectById(behavior.getId());
//        System.out.println("页面信息: " + saved.getPageInfo());
//        System.out.println("用户行为轨迹: " + saved.getBreadcrumbs());
//    }
//
//    @Test
//    public void testAllFrontendDataTogether() {
//        System.out.println("=== 测试所有前端数据类型一起插入和查询 ===");
//
//        testFrontendErrorSaveAndRead();
//        System.out.println();
//
//        testFrontendPerformanceSaveAndRead();
//        System.out.println();
//
//        testFrontendBehaviorSaveAndRead();
//        System.out.println();
//
//        System.out.println("所有前端数据测试完成！");
//    }
//
//    @Test
//    public void testFrontendData() {
//        LambdaQueryWrapper<FrontendPerformance> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(FrontendPerformance::getProjectId, "1");
//
//        List<FrontendPerformance> performances = frontendPerformanceMapper.selectList(queryWrapper);
//
//        for (FrontendPerformance performance : performances) {
//            System.out.println("event: " + performance.getEvent());
//            System.out.println(performance);
//        }
//
//    }

}