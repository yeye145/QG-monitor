package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description: 移动错误类  // 类说明
 * @ClassName: MobileError    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/12 10:51   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileError {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;

    private LocalDateTime timestamp;
    private String errorType;
    private String message;
    private String stack;
    private String className;

    private Integer event = 0;

    public synchronized void incrementEvent() {
        event++;
    }

    public synchronized Integer getEvent() {
        return event;
    }
}
