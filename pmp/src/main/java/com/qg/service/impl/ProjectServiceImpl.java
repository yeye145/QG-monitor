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
import static com.qg.utils.Constants.*;
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
    public Result addProject(PersonalProjectVO personalProjectVO) {
        // 参数校验
        if (personalProjectVO == null) {
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }

        if (personalProjectVO.getUserId() == null) {
            return new Result(Code.BAD_REQUEST, "用户ID不能为空");
        }

        int attempts = 0;
        Project project = new Project();
        Role role = new Role();

        try {
            while (attempts < 10) { // 限制尝试次数，避免无限循环
                // 生成pro-加8位随机数字的标识符
                String projectUuid = "pro-" + String.format("%08d", new Random().nextInt(100000000));
                LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
                lqw.eq(Project::getUuid, projectUuid);
                Project project1 = projectMapper.selectOne(lqw);

                if (project1 == null) {
                    // 设置项目信息
                    personalProjectVO.setUuid(projectUuid);
                    BeanUtils.copyProperties(personalProjectVO, project);

                    // 执行插入项目操作
                    int projectInsertResult = projectMapper.insert(project);
                    if (projectInsertResult != 1) {
                        return new Result(INTERNAL_ERROR, "项目创建失败");
                    }

                    // 设置角色信息
                    role.setUserId(personalProjectVO.getUserId());
                    role.setProjectId(projectUuid);
                    role.setUserRole(USER_ROLE_ADMIN);
                    role.setPower(PERMISSION_OP);

                    // 执行插入角色操作
                    int roleInsertResult = roleMapper.insert(role);
                    if (roleInsertResult != 1) {
                        // 如果角色插入失败，可以考虑回滚项目插入（这里简化处理）
                        return new Result(INTERNAL_ERROR, "角色关联失败");
                    }

                    return new Result(SUCCESS, personalProjectVO, "创建成功");
                }
                attempts++;
            }

            // 如果尝试次数过多仍未找到唯一ID
            return new Result(INTERNAL_ERROR, "生成唯一标识符失败，请重试");

        } catch (Exception e) {
            log.error("创建项目失败: userId={}", personalProjectVO.getUserId(), e);
            return new Result(INTERNAL_ERROR, "创建项目失败:");
        }
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
    public Result getPublicProjectList() {
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
    public Result getPersonalProject(Long userId) {
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, userId);
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
            lqw1.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false);
            Project project = projectMapper.selectOne(lqw1);

            if(project != null )
            {
                BeanUtils.copyProperties(role, vo);
                BeanUtils.copyProperties(project, vo);
                personalProjectVOList.add(vo);
            }
            // 复制属性
        }
        if(personalProjectVOList.isEmpty())  return new Result(Code.NOT_FOUND, "没有项目");
        return new Result(SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
    }

//    //用户获取非公开项目的列表
//    @Override
//    public Result getPersonalUnpublicProject(Long userId) {
//        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(Role::getUserId, userId );
//        List<Role> list = roleMapper.selectList(lqw);
//        if (list == null) {
//            return new Result(Code.NOT_FOUND, "用户暂无参与项目！");
//        }
//        List<PersonalProjectVO> personalProjectVOList = new ArrayList<>();
//
//        // 遍历Role列表，转换为PersonalProjectVO
//        for (Role role : list) {
//            PersonalProjectVO vo = new PersonalProjectVO();
//            String uuid = role.getProjectId();
//            LambdaQueryWrapper< Project> lqw1 = new LambdaQueryWrapper<>();
//            lqw1.eq(Project::getUuid, uuid).eq(Project::getIsDeleted, false).eq(Project::getIsPublic, false);
//            Project project = projectMapper.selectOne(lqw1);
//            if(project != null)
//            {
//                // 复制属性
//                BeanUtils.copyProperties(role, vo);
//                BeanUtils.copyProperties(project, vo);
//                personalProjectVOList.add(vo);
//            }
//
//        }
//        if(personalProjectVOList.size() == 0)  return new Result(Code.NOT_FOUND, "没有项目");
//        return new Result(SUCCESS, personalProjectVOList,"获取用户个人参与项目成功！");
//    }

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

    @Override
    public Result selectProjectByName(String name) {
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return new Result(BAD_REQUEST, "项目名称不能为空");
        }

        try {
            // 根据项目名模糊查询项目
            LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Project::getName, name.trim())
                    .eq(Project::getIsDeleted, false)
                    .orderByDesc(Project::getCreatedTime); // 按创建时间倒序排列

            List<Project> projects = projectMapper.selectList(queryWrapper);

            // 检查查询结果
            if (projects == null || projects.isEmpty()) {
                log.info("未找到匹配的项目，搜索关键词: {}", name);
                return new Result(Code.NOT_FOUND, "未找到匹配的项目");
            }

            log.info("项目搜索成功，关键词: {}，结果数量: {}", name, projects.size());
            return new Result(SUCCESS, projects, "查询项目成功");

        } catch (Exception e) {
            log.error("查询项目失败，搜索关键词: {}", name, e);
            return new Result(INTERNAL_ERROR, "查询项目失败: " + e.getMessage());
        }
    }

    @Override
    public Result getPrivateProjectList() {
        try {
            LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Project::getIsPublic, false)
                    .eq(Project::getIsDeleted,  false);
            List<Project> projects = projectMapper.selectList(queryWrapper);
            return new Result(SUCCESS, projects, "查询项目成功");
        } catch (Exception e) {
            log.error("查询项目失败", e);
            return new Result(INTERNAL_ERROR, "查询项目失败: " + e.getMessage());
        }
    }
}
