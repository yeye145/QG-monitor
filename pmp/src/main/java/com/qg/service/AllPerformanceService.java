package com.qg.service;

import com.qg.domain.Result;

/**
 * @Description: // 类说明
 * @ClassName: AllPerformanceService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 11:28   // 时间
 * @Version: 1.0     // 版本
 */
public interface AllPerformanceService {
    Result selectByCondition(String projectId, Long moduleId, String api, String environment, String capture, String deviceModel, String osVersion);
}
