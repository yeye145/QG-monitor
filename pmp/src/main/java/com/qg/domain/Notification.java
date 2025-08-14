package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    private Long receiverId;
    private LocalDateTime timestamp;
    private Integer isRead;
    private String platform;
    private String environment;
    private String errorType;
    private String content;
    private Long responsibleId;

    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
