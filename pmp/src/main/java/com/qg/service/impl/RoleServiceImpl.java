package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.domain.Role;
import com.qg.domain.Users;
import com.qg.mapper.RoleMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.RoleService;
import com.qg.vo.ProjectMemberVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.qg.utils.Constants.PERMISSION_OP;
import static com.qg.utils.Constants.USER_ROLE_ADMIN;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UsersMapper usersMapper;
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
        //判断用户是否在项目里
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, role.getUserId()).eq(Role::getProjectId, role.getProjectId());
        if (roleMapper.selectOne(lqw) == null) {
            return new Result(Code.NOT_FOUND, "该用户未加入该项目");
        }
        //更新用户权限
        return roleMapper.update(role, lqw) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
    }

    @Override
    public Result deleteRole(Role role) {
        //判断用户是否在项目里
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, role.getUserId()).eq(Role::getProjectId, role.getProjectId());
        if(roleMapper.selectOne(lqw) == null)
        {
            return new Result(Code.NOT_FOUND, "该用户未加入该项目");
        }
        //删除用户
        return roleMapper.delete(lqw) == 1 ? new Result(Code.CREATED, "删除成功") : new Result(Code.INTERNAL_ERROR, "删除失败");
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
    public Result getRole(String userId, String projectId) {
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, userId).eq(Role::getProjectId, projectId);
        Role role = roleMapper.selectOne(lqw);
        return role == null ? new Result(Code.NOT_FOUND, "该项目下无此用户") : new Result(Code.SUCCESS, role, "查询成功");
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
