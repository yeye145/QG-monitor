package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Role {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String projectId;
    private Integer power;
    private Integer userRole;
}
