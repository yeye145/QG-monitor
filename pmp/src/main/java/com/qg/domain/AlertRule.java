package com.qg.domain;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRule {
    private Long id;
    private String errorType;
    private Integer threshold;
    private String env;

}
