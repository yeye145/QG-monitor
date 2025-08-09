package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;


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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "timestamp")
    private Long timestamp;
    @TableField(value = "log_level")
    private String level;
    @TableField(value = "log_message")
    private String context;
    private String module;
    private String projectId;
    private String environment;
    private String logLevel;
    private String logMessage;
    @TableField(typeHandler = com.qg.handler.JsonbTypeHandler.class)
    private Map<String, Object> environmentSnapshot;

    private final AtomicInteger event = new AtomicInteger(0);

    // TODO: 原子性递增
    public void incrementAndGetEvent() {
        event.incrementAndGet();
    }

    // TODO: 获取当前值
    public int getEvent() {
        return event.get();
    }

}
