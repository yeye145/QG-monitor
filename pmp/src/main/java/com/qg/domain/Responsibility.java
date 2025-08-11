package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Responsibility {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String platform;
    private String projectId;
    private Long delegatorId;
    private Long responsibleId;
    private Long errorId;
    private String errorType;
    private OffsetDateTime createTime;


}
