package com.qg.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

public class Project {
    String uuid;
    String name;
    String description;
    LocalDateTime createTime;
    Boolean isPublic;

    @TableLogic
    @TableField("is_deleted")
    Boolean isDeleted;
}
