package com.qg.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    private Long id;
    private String projectId;
    private LocalDateTime timestamp;
    private String sessionId;
    private String captureType;
    private Long duration;

    private String errorType;
    private String message;
    private String stack;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> request;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> response;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> resource;
    @TableField(typeHandler = com.qg.handler.ListHandler.class)
    private List<Map<String, Object>> breadcrumbs;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> tags;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> elementInfo;

    private String userAgent;
    private String name;
    private Long responsibleId;
    private Long delegatorId;
    private String avatarUrl;
}
