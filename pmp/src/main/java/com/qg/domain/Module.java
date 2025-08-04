package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Module {
    private Long id;
    private String projectId;
    private String moduleName;
    private boolean isActive;
    private LocalDateTime createdTime;
    private LocalDateTime deletedTime;
}
