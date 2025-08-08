package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 前端性能类  // 类说明
 * @ClassName: frontendPerformance    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:15   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrontendPerformance {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Long timestamp;
    private String sessionId;
    private String userAgent;
    private String metrics;
    private String captureType;
}
