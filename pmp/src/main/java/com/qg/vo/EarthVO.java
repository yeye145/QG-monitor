package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EarthVO {
    private String ip;          // IP地址
    private String country;     // 国家名称
    private String city;        // 城市名称
    private Double latitude;    // 纬度
    private Double longitude;   // 经度
    private Integer event;      // 拦截次数
}