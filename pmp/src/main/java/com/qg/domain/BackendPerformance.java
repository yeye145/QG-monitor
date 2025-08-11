package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

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
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private LocalDateTime timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String api;
    private Integer duration;
    private Boolean slow;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> environmentSnapshot;
}
