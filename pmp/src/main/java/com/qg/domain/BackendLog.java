package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 后端日志类  // 类说明
 * @ClassName: backendLog    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:00   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendLog {
    private Long id;
    private Long timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String logLevel;
    private String logMessage;
    private String environmentSnapshot;
}
