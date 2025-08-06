package com.qg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Notification;
import com.qg.domain.Project;
import com.qg.domain.Result;
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
            queryWrapper.eq(Notification::getReceiverId, receiverId);
            List<Notification> notificationList = notificationMapper.selectList(queryWrapper);
            List<NotificationVO> notificationVOList = new ArrayList<>();
            for (Notification notification : notificationList) {
                NotificationVO notificationVO = new NotificationVO();
                BeanUtils.copyProperties(notification, notificationVO);

            }

//            log.info("成功查询通知: {}", notificationVOList);
//            return new Result(Code.SUCCESS, notificationVOList, "查询成功");
        } catch (Exception e) {
            log.error("查询通知失败，参数: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "查询通知失败: " + e.getMessage());
        }
        return null;
    }
}
