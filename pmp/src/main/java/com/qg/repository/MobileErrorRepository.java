package com.qg.repository;

import com.qg.domain.MobileError;
import com.qg.domain.Notification;
import com.qg.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static com.qg.repository.RepositoryConstants.DEFAULT_THRESHOLD;
@Slf4j
@Repository
public class MobileErrorRepository extends MobileErrorFatherRepository {

    @Autowired
    private NotificationMapper notificationMapper;
    // TODO: 告警升级前先查看《前一毫秒》是否已经解决了

    /**
     * 判断是否需要启用企业机器人发送告警
     * @param redisKey
     * @param error
     * @return
     */
    @Override
    protected boolean shouldAlert(String redisKey, MobileError error) {
        String[] data = redisKey.split(":");
        HashMap<String, Integer> alertRuleMap = alertRuleMapper
                .selectByMobileRedisKeyToMap(data[1], data[2], data[3]);

        int currentCount = error.getEvent();
        int threshold = alertRuleMap.getOrDefault(redisKey, DEFAULT_THRESHOLD.getAsInt());

        // TODO: 如果达到阈值，先检查10分钟内是否已经有相同的告警
        if(currentCount >= threshold) {
            if(checkNotificationExist("mobile", error.getProjectId()
                    , error.getId(), LocalDateTime.now())) {
                return false;
            } else {
                log.info("存入数据库");

                // TODO: 根据项目配置设置发送人和接收人ID
                // TODO: 问题1：先企业微信告警，同时异常发送到平台，管理员委派人去解决？
                // TODO: 问题2：我怎么知道前端后端异常《最近的连续10分钟内》有没有相同异常
                // TODO: 问题3：现在没有系统自带的异常，如果用户没定义，error_id将为null
                // TODO: 问题4：密码的祖传高并发，多线程情况下会重复告警
                // TODO: 问题5：现在发送信息是不是指定在《企业微信》中发送，如果不是，我应该怎么发
                // TODO: 问题6：（如果不是《全》发企业微信的话跳过此问题）我怎么知道通知已读？
                // TODO: 问题7：委派表没有逻辑删，通知表有逻辑删，以通知表逻辑删为标记解决？

                return true;
            }
        }
        return false;
    }

    // TODO: 检测通知是否已存在
    protected boolean checkNotificationExist(String type
            , String projectId, Long errorId, LocalDateTime timestamp) {
//        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Notification::, type)
//                .eq(Notification::getProjectId, projectId)
//                .eq(Notification::getErrorId, errorId)
//                .eq()

        return false;
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
                        "请及时处理！",
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName(),
                error.getEvent(),
                LocalDateTime.now()
                        .format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 存通知信息进数据库
     */
    @Override
    protected boolean saveNotification(List<Long> alertReceiverID, MobileError error) {
        int count = 0;
        for(Long receiverID : alertReceiverID){
            Notification notification = new Notification();
            notification.setProjectId(error.getProjectId());
            notification.setErrorType(error.getErrorType());
            notification.setErrorId(error.getId());
            notification.setPlatform("mobile");
            notification.setEnvironment("dev"); //TODO: 获取环境在哪里？！
            notification.setTimestamp(LocalDateTime.now());
            notification.setReceiverId(receiverID);
            notification.setSenderId(1L);
            count++;
            notificationMapper.insert(notification);
        }
        if(count == alertReceiverID.size()){
            return true;
        }
        return false;

    }
}