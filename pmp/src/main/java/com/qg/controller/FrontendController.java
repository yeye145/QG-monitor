package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.FrontendPerformance;
import com.qg.service.FrontendBehaviorService;
import com.qg.service.FrontendErrorService;
import com.qg.service.FrontendPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 前端业务类  // 类说明
 * @ClassName: FrontendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:59   // 时间
 * @Version: 1.0     // 版本
 */
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
    public String getPerformanceData(String performanceData) {
        System.out.println("***********接收到了前端性能数据***********");
        System.out.println(performanceData);
        FrontendPerformance frontendPerformance = new FrontendPerformance();
        frontendPerformance = JSONUtil.toBean(performanceData, FrontendPerformance.class);
        System.out.println("已接收的前端性能数据: " + frontendPerformance);
        return "已接收前端性能数据";
    }

}
