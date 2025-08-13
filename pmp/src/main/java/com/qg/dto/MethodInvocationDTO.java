package com.qg.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("method_invocation")
@Data
public class MethodInvocationDTO {
    private String projectId;
    private String methodName;
    private Integer event;
    private LocalDateTime createTime;
}