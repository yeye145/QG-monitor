package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private LocalDateTime timestamp;
    @TableField(value = "log_level")
    private String level;
    @TableField(value = "log_message")
    private String context;
    private String module;
    private String projectId;
    private String environment;
    private String logMessage;
    @TableField(typeHandler = com.qg.handler.MapHandler.class)
    private Map<String, Object> environmentSnapshot;

    private Integer event = 0;

    public synchronized void incrementEvent() {
        event++;
    }

    public synchronized int getEvent() {
        return event;
    }

}
