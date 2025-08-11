package com.qg.repository;

import cn.hutool.core.util.StrUtil;
import com.qg.domain.MobileError;
import com.qg.mapper.AlertRuleMapper;
import com.qg.mapper.MobileErrorMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.utils.WechatAlertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static com.qg.repository.RepositoryConstants.MOBILE_ERROR_PREFIX;
import static com.qg.repository.RepositoryConstants.TTL_MINUTES;

@Slf4j
@Repository
public abstract class MobileErrorFatherRepository extends StatisticsDataRepository<MobileError> {

    @Autowired
    protected MobileErrorMapper mobileErrorMapper;
    @Autowired
    protected AlertRuleMapper alertRuleMapper;
    @Autowired
    protected WechatAlertUtil wechatAlertUtil;
    @Autowired
    protected ProjectMapper projectMapper;


    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES.getAsInt();
    }

    @Override
    protected void saveToDatabase(MobileError error) {
        try {
            mobileErrorMapper.insert(error);
        } catch (Exception e) {
            log.error("移动端错误统计失败,项目ID: {}: {}", error.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(MobileError error) {
        return String.format("%s:%s:%s:%s",
                MOBILE_ERROR_PREFIX.getAsString(),
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName()
        );
    }

    @Override
    protected void incrementEvent(MobileError error) {
        error.incrementEvent();
    }

    protected abstract boolean shouldAlert(String redisKey, MobileError error);

    protected abstract String generateAlertMessage(MobileError error);

    /**
     * 发送微信企业机器人告警
     * @param error
     */
    protected void sendWechatAlert(MobileError error) {
        String webhookUrl = getWebhookUrl(error.getProjectId());
        if (StrUtil.isBlank(webhookUrl)) {
            log.warn("未找到对应的企业微信群机器人Webhook地址, 告警失败");
            return;
        }
        if (shouldAlert(generateUniqueKey(error), error)) {
            String message = generateAlertMessage(error);
            // TODO: 需要@的成员手机号列表
            List<String> alertReceiver = Arrays.asList(
                    "18312740985",
                    "13829142833"
            );
            // TODO: 实现从数据库查找负责人手机号逻辑
            List<String> alertReceivers = Collections.singletonList("@all");
            wechatAlertUtil.sendAlert(webhookUrl, message, alertReceiver);
        }
    }

    /**
     * 获取企业机器人webhook
     * @param projectId
     * @return
     */
    protected String getWebhookUrl(String projectId) {
        // 从数据库查询webhook
        return projectMapper.selectWebhookByProjectId(projectId);
    }


}