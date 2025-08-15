package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description: 消息类  // 类说明
 * @ClassName: Message    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/13 18:59   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long sendId;
    private Long receiverId;
    private String context;
    private String type; // 消息类型
    private LocalDateTime timestamp; // 消息发送时间
}
