package com.qg.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("method_invocation")
@Data
public class MethodInvocationVO {
    private String projectId;
    private String methodName;
    private Integer event;
    private LocalDateTime createTime;
}