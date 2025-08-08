package com.qg.controller;

import com.qg.service.MobileErrorService;
import com.qg.service.MobilePerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 移动业务类  // 类说明
 * @ClassName: MobileController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 22:01   // 时间
 * @Version: 1.0     // 版本
 */
@RequestMapping("/mobile")
@RestController
public class MobileController {


    @Autowired
    private MobilePerformanceService mobilePerformanceService;

    @Autowired
    private MobileErrorService mobileErrorService;
}
