package com.qg.service;

import com.qg.domain.MobilePerformance;
import com.qg.domain.Result;

/**
 * @Description: // 类说明
 * @ClassName: MobilePerformanceService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
public interface MobilePerformanceService {
    Integer saveMobilePerformance(MobilePerformance mobilePerformance);


    Result selectByCondition(String projectId, String deviceModel, String osVersion);
}
