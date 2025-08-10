package com.qg.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MethodInvocationVO {
    private String projectId;
    private String methodName;
    private Integer invocationCount;
    private LocalDateTime createTime;
}