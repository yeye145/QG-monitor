package com.qg.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private String uuid;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private Boolean isPublic;

    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;
}
