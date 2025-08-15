package com.qg.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.MobileError;
import com.qg.domain.Notification;
import com.qg.domain.Responsibility;
import com.qg.mapper.NotificationMapper;
import com.qg.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.qg.repository.RepositoryConstants.DEFAULT_THRESHOLD;
import static com.qg.utils.Constants.ALERT_CONTENT_NEW;

@Slf4j
@Repository
public class MobileErrorRepository extends MobileErrorFatherRepository {

    @Autowired
    private NotificationMapper notificationMapper;
    // TODO: 告警升级前先查看《前一毫秒》是否已经解决了
    @Autowired
    private NotificationService notificationService;

    /**
     * 判断是否需要启用企业机器人发送告警
     * @param redisKey
     * @param error
     * @return
     */
    @Override
    protected boolean shouldAlert(String redisKey, MobileError error) {
        log.info("判断是否应该通知");
        String[] data = redisKey.split(":");
        HashMap<String, Integer> alertRuleMap = alertRuleMapper
                .selectByMobileRedisKeyToMap(data[1], data[2], data[3]);

        int currentCount = error.getEvent();
        int threshold = alertRuleMap.getOrDefault(redisKey, DEFAULT_THRESHOLD.getAsInt());

        // TODO: 如果达到阈值，先检查40分钟内是否已经有相同的告警
        if(currentCount >= threshold) {
            if(checkNotificationExist(error, LocalDateTime.now())) {
                log.info("消息已存在或仍未被解决！");
                return false;
            } else {
                // TODO: 根据项目配置设置发送人和接收人ID
                // TODO: 问题1：先企业微信告警，同时异常发送到平台，管理员委派人去解决？
                // TODO: 问题2：我怎么知道前端后端异常《最近的连续10分钟内》有没有相同异常
                // TODO: 问题3：现在没有系统自带的异常，如果用户没定义，error_id将为null
                // TODO: 问题4：密码的祖传高并发，多线程情况下会重复告警
                // TODO: 问题5：现在发送信息是不是指定在《企业微信》中发送，如果不是，我应该怎么发
                // TODO: 问题6：（如果不是《全》发企业微信的话跳过此问题）我怎么知道通知已读？
                // TODO: 问题7：委派表没有逻辑删，通知表有逻辑删，以通知表逻辑删为标记解决？
                log.info("消息不存在，可以发送！");
                return true;
            }
        }
        return false;
    }

    // TODO: 检测通知是否已存在,同时要检测他是否被解决
    protected boolean checkNotificationExist(MobileError error, LocalDateTime timestamp) {
        //检测通知是否已存在
        log.info("错误信息：{}",error);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getErrorType, error.getErrorType())
                .eq(Notification::getProjectId, error.getProjectId())
                .eq(Notification::getErrorId, error.getId())
                .eq(Notification::getPlatform,"mobile")
                .orderByDesc(Notification::getTimestamp)  // 按时间倒序排序
                .last("LIMIT 1");  // 限制只取第一条记录
        Notification notification = notificationMapper.selectOne(queryWrapper);
        log.info("notification:{}",notification);


        if (notification == null) {
            log.info("该错误没有通知记录");
            return false;
        }else{
            // 获取notification的时间戳
            LocalDateTime notificationTime = notification.getTimestamp();
            // 计算与当前时间的差值
            Duration duration = Duration.between(notificationTime, LocalDateTime.now());
            // 判断是否超过40分钟(2400秒)
            if(duration.getSeconds() < 300){
                 log.info("该错误40分钟内有通知记录");
                //检测该错误是否未被解决 （未解决在该时间段内无需重发）
                LambdaQueryWrapper<Responsibility> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(Responsibility::getErrorType,error.getErrorType())
                        .eq(Responsibility::getProjectId,error.getProjectId());
                Responsibility responsibility1 = responsibilityMapper.selectOne(queryWrapper1);
                //若该错误未被指派、则发送警告
                if(responsibility1 == null) {
                    log.info("该错误未被指派");
                    return true;
                }
                else{
                    //已解决就要发
                    if(responsibility1.getIsHandle() == RepositoryConstants.HANDLED){
                        log.info("该错误40分钟内又报错，但其显示已解决");
                        return false;
                    }
                    else return true;
                }
            }
            log.info("该错误40分钟内无消息记录");
            return false;

        }

    }

    /**
     * 发送消息模板
     * @param error
     * @return
     */
    @Override
    protected String generateAlertMessage(MobileError error) {
        return String.format("【移动端错误告警】\n" +
                        "项目ID：%s\n" +
                        "错误类型：%s\n" +
                        "类名：%s\n" +
                        "发生次数：%d\n" +
                        "触发时间：%s\n" +
                        ALERT_CONTENT_NEW,
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName(),
                error.getEvent(),
                LocalDateTime.now()
                        .format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 构建通知信息
     */
    @Override
    protected boolean saveNotification(List<Long> alertReceiverID, MobileError error) {
        log.info("存进通知表！");
        List<Notification> notifications = new ArrayList<>();
        int count = 0;
        for(Long receiverID : alertReceiverID){
            Notification notification = new Notification();
            notification.setProjectId(error.getProjectId());
            notification.setErrorType(error.getErrorType());
            notification.setErrorId(error.getId());
            notification.setPlatform("mobile");
            notification.setTimestamp(LocalDateTime.now());
            notification.setReceiverId(receiverID);
            notification.setContent(ALERT_CONTENT_NEW);
            count++;
            notifications.add(notification);

        }
        if(count == alertReceiverID.size()){
            notificationService.add(notifications);
            log.info("已全部通知发送！");
            return true;
        }
        log.info("已通知{}个用户！",count);
        notificationService.add(notifications);
        return false;

    }
}