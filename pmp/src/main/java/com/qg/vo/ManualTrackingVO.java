package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualTrackingVO {
    private String label;  // 埋点标签（message内容）
    private Integer value; // 出现次数
}