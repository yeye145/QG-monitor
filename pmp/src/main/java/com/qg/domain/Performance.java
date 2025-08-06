package com.qg.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("\"performance\"")  // MyBatis-Plus注解
public class Performance {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private String metricName;
    private Double metricValue;
    private String unit;
    private String env;
    private LocalDateTime createdTime;
    private Integer event;

}
