package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.domain.Role;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.RoleMapper;
import com.qg.service.ProjectService;
import com.qg.service.RoleService;
import com.qg.vo.PersonalProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    RoleMapper roleMapper;

    @Override
    public Result addProject(Project project){
        int attempts = 0;
        while (attempts < 10) { // 限制尝试次数，避免无限循环
            // 生成pro-加8位随机数字的标识符
            String projectUuid = "pro-" + String.format("%08d", new java.util.Random().nextInt(100000000));
            LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Project::getUuid, projectUuid);
            Project project1 = projectMapper.selectOne(lqw);
            if(project1 == null) {
                project.setUuid(projectUuid);
                // 执行插入操作
                return projectMapper.insert(project) == 1 ? new Result(Code.CREATED, "添加成功") : new Result(Code.INTERNAL_ERROR, "添加失败");
            }
            attempts++;
        }

        // 如果尝试次数过多仍未找到唯一ID
        return new Result(Code.INTERNAL_ERROR, "生成唯一标识符失败，请重试");
    }


    //管理员修改项目
    @Override
    public Result updateProject(Project project) {
        // 创建更新条件，根据uuid进行更新
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Project::getUuid, project.getUuid());

        Project existingProject = projectMapper.selectOne(lqw);

        // 判断项目是否存在
        if (existingProject == null) {
            return new Result(Code.NOT_FOUND, "项目不存在");
        }
        // 使用update方法，传入要更新的对象和条件
        return projectMapper.update(project, lqw) == 1 ?
               new Result(Code.SUCCESS, "更新成功") :
               new Result(Code.INTERNAL_ERROR, "更新失败，项目不存在");
    }


    //删除项目
    @Override
    public Result deleteProject(String uuid) {
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false);
        if (projectMapper.selectOne(lqw) == null) {
            return new Result(Code.NOT_FOUND, "项目不存在或已被删除！");
        }
        LambdaQueryWrapper<Role> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Role::getProjectId, uuid);

        if(roleMapper.delete(lqw1)>0)
        {
            return projectMapper.delete(lqw) == 1 ? new Result(Code.SUCCESS, "删除成功") : new Result(Code.INTERNAL_ERROR, "删除失败，该项目不存在！");
        }
        return new Result(Code.INTERNAL_ERROR, "删除失败！");
    }

    //查看项目详情
    @Override
    public Result getProject(String uuid) {
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false);
        Project project = projectMapper.selectOne(lqw);
        return project != null ? new Result(Code.SUCCESS, project, "查询成功") : new Result(Code.NOT_FOUND, "项目不存在");

    }

    //获取项目列表
    @Override
    public Result getProjectList() {
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Project::getIsDeleted, false).eq(Project::getIsPublic, true);
        List<Project> list= projectMapper.selectList(lqw);
        if(list == null)
        {
            return new Result(Code.NOT_FOUND, "项目列表为空");
        }
        return new Result(Code.SUCCESS, list, "查询成功");
    }

    //用户获取公开项目的列表
    @Override
    public Result getPersonalPublicProject(Long userId) {
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, userId );
        List<Role> list = roleMapper.selectList(lqw);
        if (list == null) {
            return new Result(Code.NOT_FOUND, "用户暂无参与项目！");
        }
        List<PersonalProjectVO> personalProjectVOList = new ArrayList<>();

        // 遍历Role列表，转换为PersonalProjectVO
        for (Role role : list) {
            PersonalProjectVO vo = new PersonalProjectVO();
            String uuid = role.getProjectId();

            LambdaQueryWrapper< Project> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false).eq(Project::getIsPublic, true);
            Project project = projectMapper.selectOne(lqw1);

            if(project != null )
            {
                BeanUtils.copyProperties(role, vo);
                BeanUtils.copyProperties(project, vo);
                personalProjectVOList.add(vo);
            }
            // 复制属性
        }
        if(personalProjectVOList.size() == 0)  return new Result(Code.NOT_FOUND, "没有项目");
        return new Result(Code.SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
    }

    //用户获取非公开项目的列表
    @Override
    public Result getPersonalUnpublicProject(Long userId) {
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, userId );
        List<Role> list = roleMapper.selectList(lqw);
        if (list == null) {
            return new Result(Code.NOT_FOUND, "用户暂无参与项目！");
        }
        List<PersonalProjectVO> personalProjectVOList = new ArrayList<>();

        // 遍历Role列表，转换为PersonalProjectVO
        for (Role role : list) {
            PersonalProjectVO vo = new PersonalProjectVO();
            String uuid = role.getProjectId();
            LambdaQueryWrapper< Project> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false).eq(Project::getIsPublic, false);
            Project project = projectMapper.selectOne(lqw1);
            if(project != null)
            {
                // 复制属性
                BeanUtils.copyProperties(role, vo);
                BeanUtils.copyProperties(project, vo);
                personalProjectVOList.add(vo);
            }

        }
        if(personalProjectVOList.size() == 0)  return new Result(Code.NOT_FOUND, "没有项目");
        return new Result(Code.SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
    }
}
