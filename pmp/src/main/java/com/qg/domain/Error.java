package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    private Long id;
    private String platform;
    private Long projectId;
    private String type;
    private LocalDateTime timestamp;
    private String message;
    private String stack;
    private String userAgent;
    private String url;
    private String breadcrumbs;
    private String env;
}
