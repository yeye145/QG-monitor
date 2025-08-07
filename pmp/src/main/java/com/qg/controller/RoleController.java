package com.qg.controller;

import com.qg.domain.Result;
import com.qg.domain.Role;
import com.qg.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name ="权限判断")
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 用户添加权限
     * 管理员创建项目/用户加入项目的时候
     * @param role
     * @return
     */
    @PostMapping
    public Result addRole(@RequestBody Role role) {
        return roleService.addRole(role);
    }

    /**
     * 改变权限
     * @param role
     * @return
     */
    @PutMapping
    public Result updateRole(@RequestBody Role role) {
        return roleService.updateRole(role);
    }

    /**
     * 删除项目成员
     * @param role
     * @return
     */
    @DeleteMapping
    public Result deleteRole(@RequestBody Role role) {
        return roleService.deleteRole(role);
    }

    /**
     * 获取项目中的开发成员名单
     * @param projectId
     * @return
     */
    @GetMapping("/getMemberList")
    public  Result getMemberList(@RequestParam String projectId) {
        return roleService.getMemberList(projectId);
    }

    /**
     * 个人与项目关联
     * @param userId
     * @param projectId
     * @return
     */
    @GetMapping("/getRole")
    public Result getRole(@RequestParam String userId, @RequestParam String projectId) {
        return roleService.getRole(userId, projectId);
    }

    /**
     * 更新用户角色
     * @param role
     * @return
     */
    @PutMapping("/updateUserRole")
    public Result updateUserRole(@RequestBody Role role) {
        return roleService.updateUserRole(role);
    }

}
