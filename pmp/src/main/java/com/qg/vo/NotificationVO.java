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

    private String projectName;
    private String senderName;
    private String senderAvatar;
    private String errorType;
    private String errorMessage;

}
