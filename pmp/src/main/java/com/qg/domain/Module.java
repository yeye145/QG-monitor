package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Module {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String projectId;
    private String moduleName;
    private LocalDateTime createdTime;
    private LocalDateTime deletedTime;
}
