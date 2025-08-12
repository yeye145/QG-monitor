package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.*;
import com.qg.domain.Error;
import com.qg.dto.MobileError;
import com.qg.mapper.*;
import com.qg.service.NotificationService;
import com.qg.service.ResponsibilityService;
import com.qg.vo.ResponsibilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.qg.domain.Code.SUCCESS;

@Service
@Slf4j
public class ResponsibilityServiceImpl implements ResponsibilityService {
    @Autowired
    private ErrorMapper errorMapper;
    @Autowired
    private ResponsibilityMapper responsibilityMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private BackendErrorMapper backendErrorMapper;
    @Autowired
    private FrontendErrorMapper frontendErrorMapper;
    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Override
    public Result addResponsibility(Responsibility responsibility) {
        // 参数校验
        if (responsibility == null) {
            return new Result(Code.BAD_REQUEST, "参数错误");
        }

        if (responsibility.getProjectId() == null || responsibility.getProjectId().isEmpty()
                || responsibility.getErrorId() == null
                || responsibility.getPlatform() == null || responsibility.getPlatform().isEmpty()
                || responsibility.getResponsibleId() == null || responsibility.getDelegatorId() == null) {
            return new Result(Code.BAD_REQUEST, "参数类型不能为空");
        }

        try {
            // 判断平台类型
            switch (responsibility.getPlatform()) {
                case "backend":
                    return handleBackendResponsibility(responsibility);
                case "frontend":
                    return handleFrontendResponsibility(responsibility);
                case "mobile":
                    return handleMobileResponsibility(responsibility);
                default:
                    return new Result(Code.BAD_REQUEST, "平台类型错误");
            }
        } catch (Exception e) {
            log.error("处理责任链失败: projectId={}, errorType={}, platform={}",
                    responsibility.getProjectId(), responsibility.getErrorType(),
                    responsibility.getPlatform(), e);
            return new Result(Code.INTERNAL_ERROR, "处理责任链失败: " + e.getMessage());
        }
    }

    /**
     * 处理后端责任链
     */
    private Result handleBackendResponsibility(Responsibility responsibility) {
        // 判断错误类型是否存在
        LambdaQueryWrapper<BackendError> backendQueryWrapper = new LambdaQueryWrapper<>();
        backendQueryWrapper.eq(BackendError::getProjectId, responsibility.getProjectId())
                .eq(BackendError::getId, responsibility.getErrorId());

        BackendError backendError = backendErrorMapper.selectOne(backendQueryWrapper);
        if (backendError == null) {
            return new Result(Code.BAD_REQUEST, "后端错误类型不存在");
        }
        responsibility.setErrorType(backendError.getErrorType());
        // 处理责任链记录
        return saveOrUpdateResponsibility(responsibility, "backend");
    }

    /**
     * 处理前端责任链
     */
    private Result handleFrontendResponsibility(Responsibility responsibility) {
        // 判断错误类型是否存在
        LambdaQueryWrapper<FrontendError> frontendQueryWrapper = new LambdaQueryWrapper<>();
        frontendQueryWrapper.eq(FrontendError::getProjectId, responsibility.getProjectId())
                .eq(FrontendError::getId, responsibility.getErrorId());

        FrontendError frontendError = frontendErrorMapper.selectOne(frontendQueryWrapper);
        if (frontendError == null) {
            return new Result(Code.BAD_REQUEST, "前端错误类型不存在");
        }
        responsibility.setErrorType(frontendError.getErrorType());
        // 处理责任链记录
        return saveOrUpdateResponsibility(responsibility, "frontend");
    }

    /**
     * 处理移动端责任链
     */
    private Result handleMobileResponsibility(Responsibility responsibility) {
        // 判断错误类型是否存在
        LambdaQueryWrapper<MobileError> mobileQueryWrapper = new LambdaQueryWrapper<>();
        mobileQueryWrapper.eq(MobileError::getProjectId, responsibility.getProjectId())
                .eq(MobileError::getId, responsibility.getErrorId());

        MobileError mobileError = mobileErrorMapper.selectOne(mobileQueryWrapper);
        if (mobileError == null) {
            return new Result(Code.BAD_REQUEST, "移动端错误类型不存在");
        }
        responsibility.setErrorType(mobileError.getErrorType());
        // 处理责任链记录
        return saveOrUpdateResponsibility(responsibility, "mobile");
    }

    /**
     * 保存或更新责任链记录
     */
    private Result saveOrUpdateResponsibility(Responsibility responsibility, String platform) {
        // 判断该错误类型是否已经委派
        LambdaQueryWrapper<Responsibility> responsibilityQueryWrapper = new LambdaQueryWrapper<>();
        responsibilityQueryWrapper.eq(Responsibility::getPlatform, platform)
                .eq(Responsibility::getProjectId, responsibility.getProjectId())
                .eq(Responsibility::getErrorType, responsibility.getErrorType());

        Responsibility existResponsibility = responsibilityMapper.selectOne(responsibilityQueryWrapper);

        int result;
        if (existResponsibility == null) {
            // 新增
            result = responsibilityMapper.insert(responsibility);
            if (result > 0) {
                log.info("新增{}责任链成功，项目id：{}，错误类型：{}",
                        platform, responsibility.getProjectId(), responsibility.getErrorType());
            }
        } else {
            // 更新
            LambdaUpdateWrapper<Responsibility> responsibilityUpdateWrapper = new LambdaUpdateWrapper<>();
            responsibilityUpdateWrapper.eq(Responsibility::getProjectId, responsibility.getProjectId())
                    .eq(Responsibility::getErrorType, responsibility.getErrorType())
                    .eq(Responsibility::getPlatform, platform);
            result = responsibilityMapper.update(responsibility, responsibilityUpdateWrapper);
            if (result > 0) {
                log.info("更新{}责任链成功，项目id：{}，错误类型：{}",
                        platform, responsibility.getProjectId(), responsibility.getErrorType());
            }
        }

        if (result > 0) {
            // 发送通知到对应责任人
            try {
                List<Notification> notificationList = getNotifications(responsibility, platform);
                notificationService.add(notificationList);
            } catch (Exception e) {
                log.warn("发送通知失败，但责任链更新成功: {}", e.getMessage());
            }

            return new Result(Code.SUCCESS, "委派任务成功");
        } else {
            return new Result(Code.INTERNAL_ERROR, "委派任务失败");
        }
    }

    private static List<Notification> getNotifications(Responsibility responsibility, String platform) {
        Notification notification = new Notification();
        notification.setProjectId(responsibility.getProjectId());
        notification.setErrorType(responsibility.getErrorType());
        notification.setSenderId(responsibility.getDelegatorId());
        notification.setReceiverId(responsibility.getResponsibleId());
        notification.setPlatform(platform);

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        return notificationList;
    }


    @Override
    public Result getResponsibilityList(String projectId) {
        if (projectId == null) {
            return new Result(Code.BAD_REQUEST, "项目ID不能为空");
        }

        LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Responsibility::getProjectId, projectId);
        List<Responsibility> responsibilities = responsibilityMapper.selectList(queryWrapper);

        if (responsibilities.isEmpty()) {
            return new Result(Code.NOT_FOUND, "该项目下无责任链");
        }

        List<ResponsibilityVO> responsibilityVOList = new ArrayList<>();
        for (Responsibility responsibility : responsibilities) {
            ResponsibilityVO responsibilityVO = new ResponsibilityVO();
            BeanUtils.copyProperties(responsibility, responsibilityVO);
            fillResponsibilityVO(responsibilityVO);
            responsibilityVOList.add(responsibilityVO);
        }
        if(responsibilityVOList.size() == 0){
            return new Result(Code.NOT_FOUND, "该项目下无委派");
        }
        return new Result(SUCCESS, responsibilityVOList, "查询成功");
    }

    @Override
    public Result selectByRespId(Long responsibleId) {
        if(responsibleId == null){
            return new Result(Code.BAD_REQUEST, "用户ID不能为空");
        }
        LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Responsibility::getResponsibleId, responsibleId);
        List<Responsibility> responsibilities = responsibilityMapper.selectList(queryWrapper);

        if(responsibilities.isEmpty()){
            return new Result(Code.NOT_FOUND, "无此用户");
        }
        List<ResponsibilityVO> responsibilityVOList = new ArrayList<>();
        for (Responsibility responsibility : responsibilities) {
            ResponsibilityVO responsibilityVO = new ResponsibilityVO();
            BeanUtils.copyProperties(responsibility, responsibilityVO);
            fillResponsibilityVO(responsibilityVO);
            responsibilityVOList.add(responsibilityVO);
        }
        if(responsibilityVOList.size() == 0){
            return new Result(Code.NOT_FOUND, "此用户未被委派");
        }
        return new Result(SUCCESS, responsibilityVOList, "查询成功");
    }

    @Override
    public Result updateResponsibility(Responsibility responsibility) {
        if(responsibility.getDelegatorId() == null || responsibility.getResponsibleId() == null
                || responsibility.getProjectId() == null || responsibility.getErrorId() == null){
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }
        LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Responsibility::getProjectId, responsibility.getProjectId())
                .eq(Responsibility::getErrorId, responsibility.getErrorId());

        return responsibilityMapper.update(responsibility,queryWrapper) > 0 ? new Result(SUCCESS, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
    }

    @Override
    public Result deleteResponsibility(Long id) {
        if(id == null){
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }
        return responsibilityMapper.deleteById(id) > 0 ? new Result(SUCCESS, "删除成功") : new Result(Code.INTERNAL_ERROR, "删除失败");
    }
    //填充VO
    public ResponsibilityVO fillResponsibilityVO(ResponsibilityVO responsibilityVO) {
        // 批量收集ID
        Set<Long> errorIds = new HashSet<>();
        Set<String> projectIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();

        if (responsibilityVO.getErrorId() != null) {
            errorIds.add(responsibilityVO.getErrorId());
        }
        if (responsibilityVO.getProjectId() != null) {
            projectIds.add(responsibilityVO.getProjectId());
        }
        if (responsibilityVO.getDelegatorId() != null) {
            userIds.add(responsibilityVO.getDelegatorId());
        }
        if (responsibilityVO.getResponsibleId() != null) {
            userIds.add(responsibilityVO.getResponsibleId());
        }

        // 批量查询
        Map<Long, Error> errorMap = getErrorMap(errorIds);
        Map<String, Project> projectMap = getProjectMap(projectIds);
        Map<Long, Users> userMap = getUserMap(userIds);

        // 填充数据
        Error error = errorMap.get(responsibilityVO.getErrorId());
        if (error != null) {
            BeanUtils.copyProperties(error, responsibilityVO);
        }

        Project project = projectMap.get(responsibilityVO.getProjectId());
        if (project != null) {
            BeanUtils.copyProperties(project, responsibilityVO);
        }

        Users delegator = userMap.get(responsibilityVO.getDelegatorId());
        if (delegator != null) {
            responsibilityVO.setDelegatorName(delegator.getUsername());
            responsibilityVO.setDelegatorAvatar(delegator.getAvatar());
        }

        Users responsible = userMap.get(responsibilityVO.getResponsibleId());
        if (responsible != null) {
            responsibilityVO.setResponsibleName(responsible.getUsername());
            responsibilityVO.setResponsibleAvatar(responsible.getAvatar());
        }

        return responsibilityVO;
    }


    //批量映射
    private Map<Long, Error> getErrorMap(Set<Long> errorIds) {
        if (errorIds.isEmpty()) return Collections.emptyMap();
        LambdaQueryWrapper<Error> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Error::getId, errorIds);
        List<Error> errors = errorMapper.selectList(queryWrapper);
        return errors.stream().collect(Collectors.toMap(Error::getId, e -> e));
    }
    private Map<String, Project> getProjectMap(Set<String> projectIds) {
        if (projectIds.isEmpty()) return Collections.emptyMap();
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Project::getUuid, projectIds);
        List<Project> projects = projectMapper.selectList(queryWrapper);
        return projects.stream().collect(Collectors.toMap(Project::getUuid, p -> p));
    }

    private Map<Long, Users> getUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Users::getId, userIds);
        List<Users> users = usersMapper.selectList(queryWrapper);
        return users.stream().collect(Collectors.toMap(Users::getId, u -> u));
    }

}
