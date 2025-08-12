package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrontendPerformanceAverageVO {
    private Double fcp;          // 首次内容绘制时间(FCP)平均值
    private Double domReady;     // DOM准备完成时间平均值
    private Double loadComplete; // 页面完全加载时间平均值
}