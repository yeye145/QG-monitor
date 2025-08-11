package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;


    public Module(String projectId, String moduleName) {
        this.projectId = projectId;
        this.moduleName = moduleName;
        this.createdTime = LocalDateTime.now();
    }
}
