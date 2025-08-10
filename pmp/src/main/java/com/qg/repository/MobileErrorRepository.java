package com.qg.repository;

import com.qg.domain.MobileError;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static com.qg.repository.RepositoryConstants.DEFAULT_THRESHOLD;

@Repository
public class MobileErrorRepository extends MobileErrorFatherRepository {

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
        return currentCount >= threshold;
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
}