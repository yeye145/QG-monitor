package com.qg.service;

import com.qg.domain.Result;
import com.qg.domain.Role;

public interface RoleService {
    Result addRole(Role role);

    Result updateRole(Role role);

    Result deleteRole(Role role);

    Result getMemberList(String projectId);

    Result getProListByUserId(String userId);
}
