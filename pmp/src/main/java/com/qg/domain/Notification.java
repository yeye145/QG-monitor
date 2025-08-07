package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String projectId;
    private Long errorId;
    private Long senderId;
    private String receiverId;
    private LocalDateTime timestamp;
    private Integer isRead;
}
