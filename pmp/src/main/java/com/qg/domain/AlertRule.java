package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRule {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String errorType;
    private Integer threshold;
    private String env;

}
