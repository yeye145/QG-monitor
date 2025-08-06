package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Notification;
import com.qg.domain.Result;
import com.qg.mapper.NotificationMapper;
import com.qg.service.NotificationService;
import com.qg.vo.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

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


//
//            log.info("成功查询通知: {}", notificationVOList);
//            return new Result(Code.SUCCESS, notificationVOList, "查询成功");
        } catch (Exception e) {
            log.error("查询通知失败，参数: {}", receiverId, e);
            return new Result(Code.INTERNAL_ERROR, "查询通知失败: " + e.getMessage());
        }
        return null;
    }
}
