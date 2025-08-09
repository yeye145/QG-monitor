package com.qg.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentSnapshot {
    // 基础信息
    private String ip;
    private String protocol; // "http" 或 "https"
    private String httpMethod; // "GET"/"POST"等

    // 设备与浏览器
    private String browserName;
    private String browserVersion;
    private String osName;
    private String osArch; // x86/x64/ARM
    private String language; // 浏览器语言

    private boolean isAjax;
}
