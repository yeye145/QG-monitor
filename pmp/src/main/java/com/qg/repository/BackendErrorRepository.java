package com.qg.repository;

import com.qg.domain.BackendError;
import com.qg.domain.Notification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.qg.repository.RepositoryConstants.DEFAULT_THRESHOLD;

@Repository
public class BackendErrorRepository extends BackendErrorFatherRepository {

    // TODO: 告警升级前先查看《前一毫秒》是否已经解决了

    /**
     * 判断是否需要启用企业机器人发送告警
     * @param redisKey
     * @param error
     * @return
     */
    @Override
    protected boolean shouldAlert(String redisKey, BackendError error) {
        String[] data = redisKey.split(":");
        HashMap<String, Integer> alertRuleMap = alertRuleMapper
                .selectByBackendRedisKeyToMap(data[1], data[2], data[3]);

        int currentCount = error.getEvent();
        int threshold = alertRuleMap.getOrDefault(redisKey, DEFAULT_THRESHOLD.getAsInt());

        // TODO: 如果达到阈值，先检查10分钟内是否已经有相同的告警
        if(currentCount >= threshold) {
            if(checkNotificationNoExist("backend", error.getProjectId()
                    , error.getId(), LocalDateTime.now())) {
                return false;
            } else {
                // TODO: 如果没有相同的告警，创建Notification对象，存入缓存
                Notification notification = new Notification();
                notification.setProjectId(error.getProjectId());
                notification.setErrorId(error.getId());
                notification.setPlatform("backend");
                notification.setTimestamp(LocalDateTime.now());
                // TODO: 根据项目配置设置发送人和接收人ID
                // TODO: 问题1：先企业微信告警，同时异常发送到平台，管理员委派人去解决？
                // TODO: 问题2：我怎么知道前端后端异常《最近的连续10分钟内》有没有相同异常
                // TODO: 问题3：现在没有系统自带的异常，如果用户没定义，error_id将为null
                // TODO: 问题4：密码的祖传高并发，多线程情况下会重复告警
                // TODO: 问题5：现在发送信息是不是指定在《企业微信》中发送，如果不是，我应该怎么发
                // TODO: 问题6：（如果不是《全》发企业微信的话跳过此问题）我怎么知道通知已读？
                // TODO: 问题7：委派表没有逻辑删，通知表有逻辑删，以通知表逻辑删为标记解决？
                notification.setSenderId(1L); // 系统用户
                notification.setReceiverId(1L); // 默认接收人


                return true;
            }
        }
        return false;
    }

    protected boolean checkNotificationNoExist(String type
            , String projectId, Long errorId, LocalDateTime timestamp) {

        return true;
    }

    /**
     * 发送消息模板
     * @param error
     * @return
     */
    @Override
    protected String generateAlertMessage(BackendError error) {
        /**
         * TODO: 前端到底要发什么 未确认
         */
        return String.format("【后端错误告警】\n" +
                        "项目ID：%s\n" +
                        "错误类型：%s\n" +
                        "发生次数：%d\n" +
                        "触发时间：%s\n" +
                        "请及时处理！",
                error.getProjectId(),
                error.getErrorType(),
                error.getEvent(),
                LocalDateTime.now()
                        .format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}