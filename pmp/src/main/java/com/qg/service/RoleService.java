package com.qg.service;

import com.qg.domain.Result;
import com.qg.domain.Role;

public interface RoleService {
    Result addRole(Role role);

    Result updateRole(Role role);

    Result deleteRole(String projectId, Long userId);

    Result getMemberList(String projectId);

    Result getRole(Long userId, String projectId);

    Result updateUserRole(Role role);
}
