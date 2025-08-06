package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Performance {
    private Long id;
    String projectId;
    String metricName;
    Double metricValue;
    String unit;
    String env;
    LocalDateTime createdTime;

}
