package com.qg.dto;

import lombok.Data;

@Data
public class InviteDto {
    private Long userId;
    private String invitedCode;
}
