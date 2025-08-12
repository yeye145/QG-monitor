package com.qg.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.Responsibility;
import com.qg.domain.Users;
import com.qg.mapper.*;
import com.qg.utils.WechatAlertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.qg.repository.RepositoryConstants.BACKEND_ERROR_PREFIX;
import static com.qg.repository.RepositoryConstants.TTL_MINUTES;

@Slf4j
@Repository
public abstract class BackendErrorFatherRepository extends StatisticsDataRepository<BackendError> {

    @Autowired
    protected BackendErrorMapper backendErrorMapper;
    @Autowired
    protected AlertRuleMapper alertRuleMapper;
    @Autowired
    protected WechatAlertUtil wechatAlertUtil;
    @Autowired
    protected ProjectMapper projectMapper;
    @Autowired
    protected ResponsibilityMapper responsibilityMapper;
    @Autowired
    protected UsersMapper usersMapper;


    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES.getAsInt();
    }

    @Override
    protected void saveToDatabase(BackendError error) {
        try {
            backendErrorMapper.insert(error);
        } catch (Exception e) {
            log.error("后端错误统计失败,项目ID: {}: {}", error.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(BackendError error) {
        return String.format("%s:%s:%s:%s",
                BACKEND_ERROR_PREFIX.getAsString(),
                error.getProjectId(),
                error.getEnvironment(),
                error.getErrorType()

        );
    }

    @Override
    protected void incrementEvent(BackendError error) {
        error.incrementEvent();
    }

    protected abstract boolean shouldAlert(String redisKey, BackendError error);

    protected abstract String generateAlertMessage(BackendError error);

    /**
     * 发送微信企业机器人告警
     * @param error
     */
    protected void sendWechatAlert(BackendError error) {
        String webhookUrl = getWebhookUrl(error.getProjectId());
        if (StrUtil.isBlank(webhookUrl)) {
            log.warn("未找到对应的企业微信群机器人Webhook地址, 告警失败");
            return;
        }
        if (shouldAlert(generateUniqueKey(error), error)) {
            String message = generateAlertMessage(error);
            // TODO: 需要@的成员手机号列表

            LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Responsibility::getErrorType,error.getErrorType())
                        .eq(Responsibility::getProjectId,error.getProjectId());
            Responsibility responsibility = responsibilityMapper.selectOne(queryWrapper);

            LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Users::getId,responsibility.getResponsibleId());

            Users responsibleUser = usersMapper.selectOne(queryWrapper1);
            String responsiblePhone = responsibleUser.getPhone();
            log.info("发送告警给: {}", responsiblePhone);

            List<String> alertReceiver = Arrays.asList(responsiblePhone);

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