package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 后端性能类  // 类说明
 * @ClassName: backendPerformance    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:03   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackendPerformance {
    private Long id;
    private Long timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String api;
    private Integer duration;
    private Boolean slow;
    private String environmentSnapshot;
}
