package com.qg.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.MobileError;
import com.qg.domain.Responsibility;
import com.qg.domain.Role;
import com.qg.domain.Users;
import com.qg.mapper.*;
import com.qg.utils.WechatAlertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.qg.repository.RepositoryConstants.*;
import static com.qg.utils.Constants.USER_ROLE_ADMIN;

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
    @Autowired
    protected ResponsibilityMapper responsibilityMapper;
    @Autowired
    protected UsersMapper usersMapper;
    @Autowired
    protected RoleMapper roleMapper;


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

    protected abstract boolean saveNotification(List<Long> alertReceiverID,MobileError error);
    /**
     * 发送微信企业机器人告警
     * @param error
     */
    protected void sendWechatAlert(MobileError error) {
        log.info("发送告警");
        String webhookUrl = getWebhookUrl(error.getProjectId());
        if (StrUtil.isBlank(webhookUrl)) {
            log.warn("未找到对应的企业微信群机器人Webhook地址, 告警失败");
            return;
        }
        if (shouldAlert(generateUniqueKey(error), error)) {
            log.info("告警id为: {}", error.getId());
            String message = generateAlertMessage(error);
            if(error.getId() != null){
                // TODO: 需要@的成员手机号列表
                LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Responsibility::getErrorType,error.getErrorType())
                        .eq(Responsibility::getProjectId,error.getProjectId());

                Responsibility responsibility = responsibilityMapper.selectOne(queryWrapper);

                //TODO:标记该错误为未解决
                responsibility.setIsHandle(UN_HANDLED);
                responsibilityMapper.update(responsibility,queryWrapper);

                //存储错误数据

                //存储进通知表
                List<Long> alertReceiverID = Arrays.asList(responsibility.getResponsibleId());
                boolean success = saveNotification(alertReceiverID,error);
                if(!success){
                    log.error("保存通知进数据库失败！");
                }

                LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(Users::getId,responsibility.getResponsibleId());

                Users responsibleUser = usersMapper.selectOne(queryWrapper1);
                String responsiblePhone = responsibleUser.getPhone();
                log.info("发送告警给: {}", responsiblePhone);

                List<String> alertReceiver = Arrays.asList(responsiblePhone);


                // TODO: 实现从数据库查找负责人手机号逻辑
                List<String> alertReceivers = Collections.singletonList("@all");
                wechatAlertUtil.sendAlert(webhookUrl, message, alertReceiver);
            }else{
                LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Role::getProjectId,error.getProjectId())
                            .eq(Role::getUserRole,USER_ROLE_ADMIN);
                List<Role> roles = roleMapper.selectList(queryWrapper);

                // 2. 提取角色中的用户ID集合
                List<Long> userIds = roles.stream()
                        .map(Role::getUserId)  // 假设Role中有getUserId()
                        .collect(Collectors.toList());

                LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.in(Users::getId,userIds);
                List<Users> users = usersMapper.selectList(queryWrapper1);

                List<String> alertReceivers = users.stream()
                        .map(Users::getPhone)
                        .collect(Collectors.toList());
                log.info("发送告警给: {}", alertReceivers);

                wechatAlertUtil.sendAlert(webhookUrl, message, alertReceivers);

            }



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