package com.qg.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberVO {
    private Long id;
    private String username;
    private String avatar;
    private String userRole;
    private Integer power;

}
