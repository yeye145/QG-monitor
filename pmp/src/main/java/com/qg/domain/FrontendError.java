package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @Description: 前端错误类  // 类说明
 * @ClassName: frontendError    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:09   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrontendError {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Timestamp timestamp;
    private String sessionId;
    private String userAgent;
    private String errorType;
    private String message;
    private String stack;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String requestInfo;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String responseInfo;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String resourceInfo;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String breadcrumbs;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String tags;
    private String captureType;
    private Long duration;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String elementInfo;
}
