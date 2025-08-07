package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsibilityVO {
    private Long id;
    private String projectId;
    private Long delegatorId;
    private Long responsibleId;
    private  Long errorId;
    private LocalDateTime createdTime;

    private String projectName;

    private String delegatorName;
    private String delegatorAvatar;
    private String responsibleName;
    private String responsibleAvatar;

    private String errorType;
    private String errorMessage;
    private Integer isHandle;
}
