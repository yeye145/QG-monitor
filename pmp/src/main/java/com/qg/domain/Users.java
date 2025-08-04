package com.qg.domain;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Users {
    private Long id;
    private String username;
    private String password;
    private int role;
    private String email;
    private String avatar;
    private Boolean isDeleted;
    private LocalDateTime createTime;

}
