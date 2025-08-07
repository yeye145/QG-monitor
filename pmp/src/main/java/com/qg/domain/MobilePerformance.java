package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 移动性能类  // 类说明
 * @ClassName: MobilePerformance    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:20   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobilePerformance {
    private Long id;
    private String projectId;
    private Long timestamp;
    private String deviceModel;
    private String osVersion;
    private String batteryLevel;
    private String memoryUsage;
    private String operationFps;
}
