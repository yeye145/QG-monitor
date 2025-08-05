package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.domain.Role;
import com.qg.mapper.RoleMapper;
import com.qg.service.RoleService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
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
        return roleMapper.updateById(role) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
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
        return new Result(Code.SUCCESS,list,"查询成功");
    }

    @Override
    public Result getProListByUserId(String userId) {
        //查看个人加入的项目
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, userId);
        List<Role> list = roleMapper.selectList(lqw);
        if(list == null) {
            return new Result(Code.NOT_FOUND, "该用户未加入任何项目");
        }
        return new Result(Code.SUCCESS,list,"查询成功");
    }
}
