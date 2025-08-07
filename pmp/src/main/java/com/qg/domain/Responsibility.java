package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Responsibility {
    private Long id;
    private String projectId;
    private Long delegatorId;
    private Long responsibleId;
    private  Long errorId;
    private LocalDateTime createdTime;


}
