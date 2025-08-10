package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.Code;
import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.domain.Role;
import com.qg.dto.InviteDto;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.RoleMapper;
import com.qg.service.ProjectService;
import com.qg.service.RoleService;
import com.qg.utils.Constants;
import com.qg.utils.RedisConstants;
import com.qg.vo.PersonalProjectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.qg.domain.Code.*;
import static com.qg.utils.Constants.PERMISSION_OP;
import static com.qg.utils.Constants.USER_ROLE_MEMBER;
import static com.qg.utils.RedisConstants.INVITE_CODE_KEY;
import static com.qg.utils.RedisConstants.INVITE_CODE_TTL;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
                return projectMapper.insert(project) == 1 ? new Result(Code.CREATED, "添加成功") : new Result(INTERNAL_ERROR, "添加失败");
            }
            attempts++;
        }

        // 如果尝试次数过多仍未找到唯一ID
        return new Result(INTERNAL_ERROR, "生成唯一标识符失败，请重试");
    }


    //管理员修改项目
    @Override
    public Result updateProject(Project project) {
        log.debug("修改项目，项目信息： {}", project);

        // 直接尝试更新项目，避免先查询再更新的两次数据库访问
        LambdaUpdateWrapper<Project> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Project::getUuid, project.getUuid())
                .eq(Project::getIsDeleted, false);
        int updateResult = projectMapper.update(project, updateWrapper);

        if (updateResult == 0) {
            // 检查项目是否存在
            LambdaQueryWrapper<Project> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Project::getUuid, project.getUuid());
            Project existingProject = projectMapper.selectOne(checkWrapper);

            if (existingProject == null) {
                return new Result(Code.NOT_FOUND, "项目不存在");
            } else {
                return new Result(INTERNAL_ERROR, "更新失败");
            }
        }

        return new Result(SUCCESS, "更新成功");
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
            return projectMapper.delete(lqw) == 1 ? new Result(SUCCESS, "删除成功") : new Result(INTERNAL_ERROR, "删除失败，该项目不存在！");
        }
        return new Result(INTERNAL_ERROR, "删除失败！");
    }

    //查看项目详情
    @Override
    public Result getProject(String uuid) {
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false);
        Project project = projectMapper.selectOne(lqw);
        return project != null ? new Result(SUCCESS, project, "查询成功") : new Result(Code.NOT_FOUND, "项目不存在");

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
        return new Result(SUCCESS, list, "查询成功");
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
        return new Result(SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
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
        return new Result(SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
    }

    @Override
    public Result getInviteDCode(String projectId) {
        if (projectId == null) {
            log.error("获取项目邀请码失败，参数为空");
            return new Result(BAD_REQUEST, "获取项目邀请码失败，参数为空");
        }
        try {
            //随机生成12位的邀请码
            String inviteCode = RandomStringUtils.randomAlphanumeric(12);
            stringRedisTemplate.opsForValue().set(INVITE_CODE_KEY + inviteCode, projectId);
            //为邀请码设置十分钟有效期
            stringRedisTemplate.expire(INVITE_CODE_KEY + inviteCode, INVITE_CODE_TTL, TimeUnit.MINUTES);

            return new Result(SUCCESS, inviteCode, "获取项目邀请码成功");
        } catch (Exception e) {
            log.error("获取项目邀请码失败，项目id: {}", projectId, e);
            return new Result(INTERNAL_ERROR, "获取项目邀请码失败: " + e.getMessage());
        }
    }

    @Override
    public Result joinProject(InviteDto inviteDto) {
        if (inviteDto == null) {
            log.error("参数错误");
            return new Result(Code.BAD_REQUEST, "参数为空");
        }
        if (inviteDto.getInvitedCode() == null || inviteDto.getInvitedCode().isEmpty()
                || inviteDto.getUserId() == null ) {
            log.error("参数错误");
            return new Result(Code.BAD_REQUEST, "参数错误");
        }
        try {
            String inviteCode = inviteDto.getInvitedCode();
            Long userId = inviteDto.getUserId();
            // 从redis中取出projectId
            String projectId = stringRedisTemplate.opsForValue().get(INVITE_CODE_KEY + inviteCode);
            if (projectId == null || projectId.isEmpty()) {
                log.error("邀请码错误");
                return new Result(Code.NOT_FOUND, "邀请码错误");
            }
            // 判断项目是否存在
            Project project = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                    .eq(Project::getUuid, projectId));
            if (project == null) {
                // 不存在
                log.error("项目不存在");
                return new Result(Code.NOT_FOUND, "项目不存在");
            }
            // 判断用户是否已经是项目成员
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getUserId, userId)
                    .eq(Role::getProjectId, projectId));
            if (role != null) {
                log.info("用户已经是项目成员");
                return new Result(CONFLICT, "用户已经是项目成员");
            }
            // 邀请码正确
            // 插入数据到role表
            Role newRole = new Role();
            newRole.setProjectId(projectId);
            newRole.setUserId(userId);
            newRole.setUserRole(USER_ROLE_MEMBER);
            newRole.setPower(PERMISSION_OP);
            int insert = roleMapper.insert(newRole);
            if (insert == 1) {
                log.info("用户加入项目成功");
                return new Result(SUCCESS, newRole, "用户加入项目成功");
            } else {
                log.error("用户加入项目失败: userId={}, projectId={}", userId, projectId);
                return new Result(INTERNAL_ERROR, "用户加入项目失败");
            }
        } catch (Exception e) {
            log.error("加入项目失败");
            return new Result(INTERNAL_ERROR, "加入项目失败");
        }
    }
}
