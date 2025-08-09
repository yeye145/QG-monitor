package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private Long id;
    private String projectId;
    private Long errorId;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime timestamp;
    private Integer isRead;
    private String type;
    private String environment;

    private String projectName;//项目名

    private String senderName;//发送者
    private String senderAvatar;

    private String errorType;
    private String errorMessage;
}
