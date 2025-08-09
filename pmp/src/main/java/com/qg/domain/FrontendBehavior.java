package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @Description: 前端行为类  // 类说明
 * @ClassName: frontendBahavior    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:07   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrontendBehavior {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Timestamp timestamp;
    private String sessionId;
    private String userAgent;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String breadcrumbs;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private String pageInfo;
    private String captureType;
}
