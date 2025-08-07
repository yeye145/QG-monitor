package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Error;
import com.qg.mapper.ErrorMapper;
import com.qg.mapper.NotificationMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.NotificationService;
import com.qg.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.utils.RedisConstants.IS_NOT_READ;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ErrorMapper errorMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Result selectByReceiverId(Integer receiverId) {
        if (receiverId == null) {
            log.error("查询通知信息失败，接收者id参数为空");
            return new Result(Code.BAD_REQUEST, "查询通知信息失败，接收者id参数为空");
        }

        try {
            // 查询通知列表
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getReceiverId, receiverId)
                    .eq(Notification::getIsRead, IS_NOT_READ)
                    .orderByDesc(Notification::getTimestamp); // 按创建时间倒序
            List<Notification> notificationList = notificationMapper.selectList(queryWrapper);

            if (notificationList.isEmpty()) {
                log.info("接收者ID {} 未查询到通知信息", receiverId);
                return new Result(Code.SUCCESS, new ArrayList<>(), "暂无通知信息");
            }

            // 批量查询关联数据以提高性能
            List<NotificationVO> notificationVOList = convertToVOList(notificationList);

            log.info("成功查询通知，接收者ID: {}, 数量: {}", receiverId, notificationVOList.size());
            return new Result(Code.SUCCESS, notificationVOList, "查询成功");
        } catch (Exception e) {
            log.error("查询通知失败，接收者ID: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "查询通知失败: " + e.getMessage());
        }
    }

    /**
     * 将通知列表转换为VO列表
     */
    private List<NotificationVO> convertToVOList(List<Notification> notificationList) {
        if (notificationList.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取所有需要查询的ID
        List<String> projectIds = new ArrayList<>();
        List<Long> senderIds = new ArrayList<>();
        List<Long> errorIds = new ArrayList<>();

        for (Notification notification : notificationList) {
            if (notification.getProjectId() != null && !projectIds.contains(notification.getProjectId())) {
                projectIds.add(notification.getProjectId());
            }
            if (notification.getSenderId() != null && !senderIds.contains(notification.getSenderId())) {
                senderIds.add(notification.getSenderId());
            }
            if (notification.getErrorId() != null && !errorIds.contains(notification.getErrorId())) {
                errorIds.add(notification.getErrorId());
            }
        }

        // 批量查询关联数据
        Map<String, Project> projectMap = getProjectMap(projectIds);
        Map<Long, Users> userMap = getUserMap(senderIds);
        Map<Long, Error> errorMap = getErrorMap(errorIds);

        // 转换为VO对象
        List<NotificationVO> notificationVOList = new ArrayList<>();
        for (Notification notification : notificationList) {
            NotificationVO notificationVO = new NotificationVO();
            BeanUtils.copyProperties(notification, notificationVO);

            // 设置关联信息
            if (notification.getProjectId() != null) {
                Project project = projectMap.get(notification.getProjectId());
                if (project != null) {
                    notificationVO.setProjectName(project.getName());
                }
            }

            if (notification.getSenderId() != null) {
                Users sender = userMap.get(notification.getSenderId());
                if (sender != null) {
                    notificationVO.setSenderName(sender.getUsername());
                    notificationVO.setSenderAvatar(sender.getAvatar());
                }
            }

            if (notification.getErrorId() != null) {
                Error error = errorMap.get(notification.getErrorId());
                if (error != null) {
                    notificationVO.setErrorType(error.getType());
                    notificationVO.setErrorMessage(error.getMessage());
                }
            }
            notificationVOList.add(notificationVO);
        }
        return notificationVOList;
    }

    /**
     * 批量获取项目信息映射
     */
    private Map<String, Project> getProjectMap(List<String> projectIds) {
        if (projectIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Project::getUuid, projectIds);
        List<Project> projects = projectMapper.selectList(queryWrapper);

        return projects.stream()
                .collect(Collectors.toMap(Project::getUuid, project -> project));
    }

    /**
     * 批量获取用户信息映射
     */
    private Map<Long, Users> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Users::getId, userIds);
        List<Users> users = usersMapper.selectList(queryWrapper);

        return users.stream()
                .collect(Collectors.toMap(Users::getId, user -> user));
    }

    /**
     * 批量获取错误信息映射
     */
    private Map<Long, Error> getErrorMap(List<Long> errorIds) {
        if (errorIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<Error> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Error::getId, errorIds);
        List<Error> errors = errorMapper.selectList(queryWrapper);

        return errors.stream()
                .collect(Collectors.toMap(Error::getId, error -> error));
    }
}
