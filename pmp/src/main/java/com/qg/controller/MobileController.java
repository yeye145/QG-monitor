package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.MobileError;
import com.qg.domain.MobilePerformance;
import com.qg.service.MobileErrorService;
import com.qg.service.MobilePerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@Slf4j
public class MobileController {


    @Autowired
    private MobilePerformanceService mobilePerformanceService;

    @Autowired
    private MobileErrorService mobileErrorService;


    @PostMapping("/performance")
    public void getPerformanceData(@RequestBody String performanceData) {
        log.info("***********接收到了移动端性能数据***********");
        log.info(performanceData);
        MobilePerformance mobilePerformance = JSONUtil.toBean(performanceData, MobilePerformance.class);// 解析JSON数据
        if (mobilePerformanceService.saveMobilePerformance(mobilePerformance) > 0) {
            log.info("已接收的移动端性能数据: " + mobilePerformance);
        } else {
            log.error("接收移动端性能数据失败");
        }
    }

    @PostMapping("/error")
    public void getErrorData(@RequestBody String mobileErrorJSON) {
        mobileErrorService.receiveErrorFromSDK(mobileErrorJSON);
    }

}
