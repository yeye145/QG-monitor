package com.qg.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsersDto {
    private Long id;
    private String username;
    private int role;
    private String avatar;
}
