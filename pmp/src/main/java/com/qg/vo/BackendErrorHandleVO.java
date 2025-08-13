package com.qg.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BackendErrorHandleVO {
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

    private Integer isHandle;
}
