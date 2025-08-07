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
import java.util.List;

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
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getReceiverId, receiverId)
                        .eq(Notification::getIsRead, 0);
            List<Notification> notificationList = notificationMapper.selectList(queryWrapper);
            List<NotificationVO> notificationVOList = new ArrayList<>();
            for (Notification notification : notificationList) {
                NotificationVO notificationVO = new NotificationVO();
                BeanUtils.copyProperties(notification, notificationVO);
                //创建方法搜索三个表填充该VO
                fillNotificationVO(notificationVO);
                //把生成好的VO信息填充到list里面
                notificationVOList.add(notificationVO);
            }

//            log.info("成功查询通知: {}", notificationVOList);
//            return new Result(Code.SUCCESS, notificationVOList, "查询成功");
        } catch (Exception e) {
            log.error("查询通知失败，参数: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "查询通知失败: " + e.getMessage());
        }
        return null;
    }

    //查询三个表后进行填充
    public NotificationVO fillNotificationVO(NotificationVO notificationVO){
        String projectId = notificationVO.getProjectId();
        Long errorId = notificationVO.getErrorId();
        String senderId = notificationVO.getSenderId();
        LambdaQueryWrapper<Project> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Project::getUuid, projectId);
        Project project = projectMapper.selectOne(queryWrapper1);
        if(project != null){
            notificationVO.setProjectName(project.getName());
        }
        LambdaQueryWrapper<Users> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Users::getId, senderId);
        Users users = usersMapper.selectOne(queryWrapper2);
        if(users != null){
            notificationVO.setSenderName(users.getUsername());
            notificationVO.setSenderAvatar(users.getAvatar());
        }
        LambdaQueryWrapper<Error> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Error::getId, errorId);
        Error error = errorMapper.selectOne(queryWrapper3);
        if(error != null){
            notificationVO.setErrorType(error.getType());
            notificationVO.setErrorMessage(error.getMessage());
        }

        return notificationVO;

    }

    @Override
    public Result addNotification(Notification notification) {
        return null;
    }
}
