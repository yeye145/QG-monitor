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
public class Error {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String platform;
    private String projectId;
    private String type;
    private LocalDateTime timestamp;
    private String message;
    private String stack;
    private String userAgent;
    private String url;
    private String breadcrumbs;
    private String env;
    private Long moduleId;
    // 发生次数
    private Integer event;
    // 是否已处理
    private Integer isHandled;
}
