package com.qg.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @Description: 前端错误Vo  // 类说明
 * @ClassName: FrontendResponsibilityVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:20   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrontendResponsibilityVO {

    private String projectId;
    private LocalDateTime timestamp;
    private String sessionId;
    private String userAgent;
    private String errorType;
    private String message;
    private String stack;
    private String requestInfo;
    private String responseInfo;
    private String resourceInfo;
    private String breadcrumbs;
    private String tags;
    private String captureType;
    private Long duration;
    private String elementInfo;

    private String Name;
    private Long delegatorId;
    private String avatarUrl;
}
