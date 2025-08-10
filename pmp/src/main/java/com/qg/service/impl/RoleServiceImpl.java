package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.RoleMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.RoleService;
import com.qg.vo.ProjectMemberVO;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.qg.utils.Constants.*;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Override
    public Result addRole(Role role) {
        //判断该用户是否在项目中
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, role.getUserId()).eq(Role::getProjectId, role.getProjectId());
        if (roleMapper.selectOne(lqw) != null) {
            return new Result(Code.CONFLICT, "该用户已加入该项目");
        }
        //加入项目
        return roleMapper.insert(role) == 1 ? new Result(Code.CREATED, "添加成功") : new Result(Code.INTERNAL_ERROR, "添加失败");
    }

    @Override
    public Result updateRole(Role role) {
        // 参数校验
        if (role == null) {
            return new Result(Code.BAD_REQUEST, "参数错误");
        }

        if (role.getUserId() == null || role.getProjectId() == null) {
            return new Result(Code.BAD_REQUEST, "用户ID和项目ID不能为空");
        }

        try {
            // 构建更新条件
            LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Role::getUserId, role.getUserId())
                    .eq(Role::getProjectId, role.getProjectId());

            // 直接执行更新操作
            int updateResult = roleMapper.update(role, lqw);

            if (updateResult > 0) {
                log.info("更新用户角色成功: userId={}, projectId={}",
                        role.getUserId(), role.getProjectId());
                return new Result(Code.SUCCESS, "更新成功");
            } else {
                log.info("更新用户角色失败，用户未加入该项目: userId={}, projectId={}",
                        role.getUserId(), role.getProjectId());
                return new Result(Code.NOT_FOUND, "该用户未加入该项目");
            }
        } catch (Exception e) {
            log.error("更新用户角色失败: userId={}, projectId={}",
                    role.getUserId(), role.getProjectId(), e);
            return new Result(Code.INTERNAL_ERROR, "更新失败: " + e.getMessage());
        }
    }


    @Override
    public Result deleteRole(String projectId, Long userId) {
        // 参数校验

        if (projectId == null || userId == null) {
            return new Result(Code.BAD_REQUEST, "用户ID和项目ID不能为空");
        }

        try {
            // 构建删除条件
            LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Role::getUserId, userId)
                    .eq(Role::getProjectId, projectId);

            // 直接执行删除操作
            int deleteResult = roleMapper.delete(lqw);

            if (deleteResult > 0) {
                log.info("删除用户角色成功，用户id： {}", userId);
                return new Result(Code.SUCCESS, "删除成功");
            } else {
                log.info("删除用户角色失败，用户id： {}", userId);
                return new Result(Code.NOT_FOUND, "该用户未加入该项目");
            }
        } catch (Exception e) {
            log.error("删除用户角色失败: userId={}, projectId={}",
                    userId, projectId, e);
            return new Result(Code.INTERNAL_ERROR, "删除失败: " + e.getMessage());
        }
    }


    @Override
    public Result getMemberList(String projectId) {
        //获取项目成员
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getProjectId, projectId);
        List<Role> list = roleMapper.selectList(lqw);
        if(list == null) {
            return new Result(Code.NOT_FOUND, "该项目下无成员或项目不存在");
        }
        List<ProjectMemberVO> projectMemberVOList = new ArrayList<>();
        for(Role role : list)
        {
            ProjectMemberVO vo = new ProjectMemberVO();
            Long id= role.getUserId();
            vo.setId(id);
            LambdaQueryWrapper<Users> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(Users::getId, id);
            Users users = usersMapper.selectOne(lqw1);
            if(users != null){
                BeanUtils.copyProperties(users,vo);
                BeanUtils.copyProperties(role,vo);
                projectMemberVOList.add(vo);
            }

        }
        if(projectMemberVOList.size() == 0){
            return new Result(Code.NOT_FOUND, "该项目下无成员");
        }
        return new Result(Code.SUCCESS,projectMemberVOList,"查询成功");
    }


    //查询该项目该用户的角色与权限
    @Override
    public Result getRole(Long userId, String projectId) {
        // 参数校验
        if (userId == null || projectId == null || projectId.isEmpty()) {
            log.warn("参数为空: userId={}, projectId={}", userId, projectId);
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }

        try {
            // 查询项目信息
            LambdaQueryWrapper<Project> projectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            projectLambdaQueryWrapper.eq(Project::getUuid, projectId)
                    .eq(Project::getIsDeleted, false);
            Project project = projectMapper.selectOne(projectLambdaQueryWrapper);

            // 检查项目是否存在
            if (project == null) {
                log.warn("项目不存在或已被删除: projectId={}", projectId);
                return new Result(Code.NOT_FOUND, "项目不存在或已被删除");
            }

            // 查询用户在项目中的角色
            LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(Role::getUserId, userId)
                    .eq(Role::getProjectId, projectId);
            Role role = roleMapper.selectOne(roleQueryWrapper);

            // 如果用户在项目中没有角色，创建默认角色
            if (role == null) {
                Role defaultRole = new Role();
                defaultRole.setUserId(userId);
                defaultRole.setProjectId(projectId);

                // 根据项目是否公开设置默认权限
                if (project.getIsPublic()) {
                    defaultRole.setPower(PERMISSION_READ); // 公开项目默认权限
                } else {
                    defaultRole.setPower(PERMISSION_NOT_VISIBLE); // 私有项目默认权限
                }

                log.info("为用户创建默认角色: userId={}, projectId={}, power={}",
                        userId, projectId, defaultRole.getPower());
                return new Result(Code.SUCCESS, defaultRole, "查询成功");
            }

            log.info("查询到用户角色: userId={}, projectId={}, power={}",
                    userId, projectId, role.getPower());
            return new Result(Code.SUCCESS, role, "查询成功");

        } catch (Exception e) {
            log.error("查询用户角色时发生异常: userId={}, projectId={}", userId, projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询用户角色失败: " + e.getMessage());
        }
    }


    @Override
    public Result updateUserRole(Role role) {
        if(role.getId() == null){
            return new Result(Code.BAD_REQUEST, "参数id为空！");
        }
        if(role.getUserRole() == USER_ROLE_ADMIN){
            role.setPower(PERMISSION_OP);
            return roleMapper.updateById(role) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
        }
        else{
            return roleMapper.updateById(role) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
        }
    }
}
