package com.qg.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class FrontendErrorHandleVO {
    private Long id;
    private String projectId;
    private LocalDateTime timestamp;
    private String sessionId;
    private String userAgent;
    private String errorType;
    private String message;
    private String stack;

    private Integer event;

    @TableField(typeHandler = com.qg.handler.MapHandler.class, value = "request_info")
    private Map<String, Object> request;
    @TableField(typeHandler = com.qg.handler.MapHandler.class, value = "response_info")
    private Map<String, Object> response;
    @TableField(typeHandler = com.qg.handler.MapHandler.class, value = "resource_info")
    private Map<String, Object> resource;
    @TableField(typeHandler = com.qg.handler.ListHandler.class)
    private List<Map<String, Object>> breadcrumbs;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> tags;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> elementInfo;
    private String captureType;
    private Long duration;

    private Integer isHandle;
}
