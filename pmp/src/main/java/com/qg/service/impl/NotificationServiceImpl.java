package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.*;
import com.qg.domain.Error;
import com.qg.mapper.ErrorMapper;
import com.qg.mapper.NotificationMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.UsersMapper;
import com.qg.service.NotificationService;
import com.qg.vo.NotificationVO;
import com.qg.websocket.UnifiedWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.utils.RedisConstants.*;

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

    @Autowired
    private UnifiedWebSocketHandler webSocketHandler;

    @Override
    public Result selectByReceiverId(Long receiverId, Integer isSenderExist) {
        if (receiverId == null || isSenderExist == null
                || !isSenderExist.equals(IS_SENDER_EXIST) && !isSenderExist.equals(IS_SENDER_NOT_EXIST)) {
            log.error("查询通知信息失败，参数错误");
            return new Result(Code.BAD_REQUEST, "查询通知信息失败，参数错误");
        }

        try {
            // 查询通知列表
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getReceiverId, receiverId)
                    .orderByAsc(Notification::getIsRead) // 未读(0)在前，已读(1)在后
                    .orderByDesc(Notification::getTimestamp); // 按创建时间倒序

            if (isSenderExist.equals(IS_SENDER_EXIST)) {
                // 如果存在发送者
                queryWrapper.isNotNull(Notification::getSenderId);
            } else {
                // 如果不存在发送者
                queryWrapper.isNull(Notification::getSenderId);
            }
            List<Notification> notificationList = notificationMapper.selectList(queryWrapper);

            if (notificationList.isEmpty()) {
                log.info("接收者ID {} 未查询到通知信息", receiverId);
                return new Result(Code.SUCCESS, new ArrayList<>(), "暂无通知信息");
            }

            // 批量查询关联数据以提高性能
            List<NotificationVO> notificationVOList = convertToVOList(notificationList);

            // 统计未读和已读数量
            long unreadCount = notificationList.stream()
                    .filter(notification -> IS_NOT_READ.equals(notification.getIsRead()))
                    .count();
            long readCount = notificationList.size() - unreadCount;

            log.info("成功查询通知，接收者ID: {}, 总数: {}, 未读: {}, 已读: {}",
                    receiverId, notificationVOList.size(), unreadCount, readCount);
            return new Result(Code.SUCCESS, notificationVOList, "查询成功");
        } catch (Exception e) {
            log.error("查询通知失败，接收者ID: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "查询通知失败: " + e.getMessage());
        }
    }

    @Override
    public Result updateIsRead(Long receiverId) {
        if (receiverId == null) {
            log.error("更新通知已读状态失败，参数为空");
            return new Result(Code.BAD_REQUEST, "更新通知已读状态失败，参数为空");
        }
        try {
            LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Notification::getReceiverId, receiverId)
                    .eq(Notification::getIsRead, IS_NOT_READ)
                    .set(Notification::getIsRead, IS_READ);
            int updateCount = notificationMapper.update(null, updateWrapper);
            log.info("更新通知已读状态成功，更新数量: {}", updateCount);
            return new Result(Code.SUCCESS, updateCount, "更新成功");
        } catch (Exception e) {
            log.error("更新通知已读状态失败，参数: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "更新通知已读状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result add(List<Notification> notificationList) {
        if (notificationList == null || notificationList.isEmpty()) {
            log.error("添加通知失败，参数为空");
            return new Result(Code.BAD_REQUEST, "添加通知失败，参数为空");
        }

        try {
            log.debug("开始批量添加通知，数量: {}", notificationList.size());
            int successCount = 0;
            List<Notification> insertedNotifications = new ArrayList<>();

            // 分类存储通知
            List<Notification> generalNotifications = new ArrayList<>();  // 无senderId的通知
            List<Notification> designateNotifications = new ArrayList<>(); // 有senderId的通知

            // 批量插入通知
            for (Notification notification : notificationList) {
                if (notification == null) {
                    log.warn("跳过空的通知对象");
                    continue;
                }

                // 设置时间戳
                notification.setTimestamp(LocalDateTime.now());

                int result = notificationMapper.insert(notification);
                if (result > 0) {
                    successCount++;
                    insertedNotifications.add(notification);

                    // 根据senderId是否存在进行分类
                    if (notification.getSenderId() != null) {
                        designateNotifications.add(notification);
                        log.debug("添加指定通知成功: {}", notification);
                    } else {
                        generalNotifications.add(notification);
                        log.debug("添加通用通知成功: {}", notification);
                    }
                } else {
                    log.warn("添加通知失败: {}", notification);
                }
            }

            if (successCount > 0) {
                log.info("批量添加通知完成，总数量: {}，成功: {}，通用通知: {}，指定通知: {}",
                        notificationList.size(), successCount,
                        generalNotifications.size(), designateNotifications.size());

                // 分别广播不同类型的通知
                if (!generalNotifications.isEmpty()) {
                    broadcastNotificationsByType(generalNotifications, "notifications");
                }

                if (!designateNotifications.isEmpty()) {
                    broadcastNotificationsByType(designateNotifications, "designate");
                }

                return new Result(Code.SUCCESS,
                        String.format("批量添加通知成功，共处理 %d 条，成功 %d 条，通用 %d 条，指定 %d 条",
                                notificationList.size(), successCount,
                                generalNotifications.size(), designateNotifications.size()));
            } else {
                log.error("批量添加通知全部失败，总数量: {}", notificationList.size());
                return new Result(Code.INTERNAL_ERROR, "批量添加通知失败");
            }
        } catch (Exception e) {
            log.error("批量添加通知失败，通知列表: {}", notificationList, e);
            return new Result(Code.INTERNAL_ERROR, "批量添加通知失败: " + e.getMessage());
        }
    }

    @Override
    public Result updateIsReadById(Long id) {
        if (id == null) {
            log.error("更新通知失败，参数为空");
            return new Result(Code.BAD_REQUEST, "更新通知失败，参数为空");
        }
        try {
            LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Notification::getId, id)
                    .set(Notification::getIsRead, IS_READ);
            int result = notificationMapper.update(null, updateWrapper);
            if (result > 0) {
                return new Result(Code.SUCCESS, "更新成功");
            } else {
                return new Result(Code.INTERNAL_ERROR, "更新失败");
            }
        } catch (Exception e) {
            log.error("更新通知失败，通知ID: {}", id, e);
            return new Result(Code.INTERNAL_ERROR, "更新通知失败: " + e.getMessage());
        }
    }

    /**
     * 按类型广播通知
     * @param notifications 通知列表
     * @param messageType 消息类型 ("notifications" 或 "designate")
     */
    private void broadcastNotificationsByType(List<Notification> notifications, String messageType) {
        try {
            if (notifications.isEmpty()) {
                return;
            }

            // 批量转换为 VO 对象
            List<NotificationVO> notificationVOList = convertToVOList(notifications);

            // 创建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", messageType);
            message.put("data", notificationVOList);
            message.put("count", notificationVOList.size());
            message.put("timestamp", System.currentTimeMillis());

            // 推送给前端
            webSocketHandler.sendMessageByType(messageType, message);

            log.debug("批量{}通知广播成功，数量: {}",
                    "notifications".equals(messageType) ? "通用" : "指定",
                    notifications.size());
        } catch (Exception e) {
            log.error("批量广播{}通知失败，通知数量: {}",
                    "notifications".equals(messageType) ? "通用" : "指定",
                    notifications.size(), e);
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
