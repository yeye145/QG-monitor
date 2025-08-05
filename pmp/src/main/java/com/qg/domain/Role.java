package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Role {
    private Long id;
    private Long userId;
    private String username;
    private String projectId;
    private String name;
    private String power;

}
