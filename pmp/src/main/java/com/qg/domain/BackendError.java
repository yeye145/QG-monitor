package com.qg.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Description: 后端错误类  // 类说明
 * @ClassName: backendError    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 20:56   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendError {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private LocalDateTime timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String errorType;
    private String stack;
    @TableField(value = "environment_snapshot", typeHandler = com.qg.handler.MapHandler.class,jdbcType = JdbcType.OTHER)
    private Map<String, Object> environmentSnapshot;
    private Integer event;
}
