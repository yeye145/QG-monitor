package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Error;
import com.qg.mapper.ErrorMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.ResponsibilityMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.NotificationService;
import com.qg.service.ResponsibilityService;
import com.qg.vo.ResponsibilityVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
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

    @Override
    public Result addResponsibility(Responsibility responsibility) {
        LambdaQueryWrapper<Error> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Error::getProjectId, responsibility.getProjectId())
                .eq(Error::getId, responsibility.getErrorId());
        Error error = errorMapper.selectOne(queryWrapper);
        if (error == null) {
            return new Result(Code.NOT_FOUND, "未找到该错误信息");
        }

        // 检查是否已存在相同的责任链记录
        LambdaQueryWrapper<Responsibility> responsibilityQueryWrapper = new LambdaQueryWrapper<>();
        responsibilityQueryWrapper.eq(Responsibility::getProjectId, responsibility.getProjectId())
                .eq(Responsibility::getErrorId, responsibility.getErrorId());

        Responsibility existing = responsibilityMapper.selectOne(responsibilityQueryWrapper);
        if (existing != null) {
            return new Result(Code.CONFLICT, "该错误已被分配");
        }
        boolean success = responsibilityMapper.insert(responsibility) > 0 ;
        if (success) {

            Notification notification = new Notification();
            notification.setProjectId(responsibility.getProjectId());
            notification.setErrorId(responsibility.getErrorId());
            notification.setSenderId(responsibility.getDelegatorId());
            notification.setReceiverId(responsibility.getResponsibleId());
            List<Notification> notificationList = new ArrayList<>();
            notificationList.add(notification);
            notificationService.add(notificationList);

            return  new Result(Code.SUCCESS, "添加责任链成功");
        }
        else{
            return new Result(Code.INTERNAL_ERROR, "添加责任链失败");
        }

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
        return new Result(Code.SUCCESS, responsibilityVOList, "查询成功");
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
        return new Result(Code.SUCCESS, responsibilityVOList, "查询成功");
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

        return responsibilityMapper.update(responsibility,queryWrapper) > 0 ? new Result(Code.SUCCESS, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
    }

    @Override
    public Result deleteResponsibility(Long id) {
        if(id == null){
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }
        return responsibilityMapper.deleteById(id) > 0 ? new Result(Code.SUCCESS, "删除成功") : new Result(Code.INTERNAL_ERROR, "删除失败");
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
