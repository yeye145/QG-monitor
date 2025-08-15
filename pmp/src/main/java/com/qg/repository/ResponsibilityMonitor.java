package com.qg.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.mapper.*;
import com.qg.service.NotificationService;
import com.qg.utils.WechatAlertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.qg.utils.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponsibilityMonitor {

    @Autowired
    private ResponsibilityMapper responsibilityMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private WechatAlertUtil wechatAlertUtil;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private MobileErrorMapper mobileErrorMapper;
    @Autowired
    private FrontendErrorMapper frontendErrorMapper;
    @Autowired
    private BackendErrorMapper backendErrorMapper;
    @Autowired
    private UsersMapper usersMapper;

    // 每1分钟检查一次(可根据需要调整)
    //@Scheduled(fixedRate = 1 * 60 * 1000)
    @Transactional
    public void checkUnhandledResponsibilities() {
        log.info("开始检查未处理的责任项...");

        // 1. 查询所有未处理的责任项
        List<Responsibility> unhandledItems = findByIsHandle();
        log.info("查询到未处理的责任项：{}", unhandledItems);

        if (unhandledItems.isEmpty()) {
            log.info("没有未处理的责任项");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int notificationCount = 0;

        // 2. 遍历每个未处理项
        for (Responsibility item : unhandledItems) {
            LocalDateTime updateTime = item.getUpdateTime();
            Duration duration = Duration.between(updateTime, now);
            long minutesPassed = duration.toMinutes();

            // 3. 检查是否超过40分钟或80分钟
            if (minutesPassed >= ALERT_UPGRADE_TIME_BOSS ) {
                log.info("触发老板报警！");
                //获取上次信息接收者角色
                Integer userRole = notificationSendto(item);
                if(userRole == null){
                    log.info("获取信息失败，直接发给老板");
                    //发送给成员
                    List<Notification> notificationList2 = buildResponsibilityList(item, USER_ROLE_MEMBER);
                    if (notificationList2.isEmpty()){
                        log.warn("该项目无成员，无法再发送！");
                    }
                    //发微信
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_MEMBER));

                    //发送给老板
                    //TODO:发送信息
                    List<Notification> notificationList =buildResponsibilityList( item, USER_ROLE_BOSS);

                    //TODO:发送微信
                    if(notificationList.isEmpty() ) {
                        log.warn("该项目无老板，无法再发送！上报给管理员");
                        List<Notification> notificationList1 = buildResponsibilityList( item, USER_ROLE_ADMIN);
                        //发信息

                        List<Notification> allNotifications = Stream.concat(
                                notificationList2.stream(),
                                notificationList1.stream()
                        ).collect(Collectors.toList());
                        //发信息
                        notificationService.add(allNotifications);

                        //发微信
                        wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                                , buildMentionedMobileList(item, USER_ROLE_ADMIN));
                    }
                    //发信息
                    List<Notification> allNotifications = Stream.concat(
                            notificationList2.stream(),
                            notificationList.stream()
                    ).collect(Collectors.toList());
                    //发信息
                    notificationService.add(allNotifications);
                    //发微信
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_BOSS));

                }else if(userRole.equals(USER_ROLE_ADMIN)){

                    log.info("获取信息成功，上次发给了管理员，升级报警，发送给老板");
                    //TODO:发送信息
                    //发送给成员
                    List<Notification> notificationList2 = buildResponsibilityList(item, USER_ROLE_MEMBER);
                    if (notificationList2.isEmpty()){
                        log.warn("该项目无成员，无法再发送！");
                    }
                    //发微信
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_MEMBER));

                    //发送给老板
                    List<Notification> notificationList =buildResponsibilityList( item, USER_ROLE_BOSS);
                    if(notificationList.isEmpty()){
                        log.warn("该项目无老板，无法再发送！上报给管理员");
                        List<Notification> notificationList1 = buildResponsibilityList( item, USER_ROLE_ADMIN);
                        List<Notification> allNotifications = Stream.concat(
                                notificationList1.stream(),
                                notificationList.stream()
                        ).collect(Collectors.toList());
                        //发信息
                        notificationService.add(allNotifications);

                        //发微信
                        wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                                , buildMentionedMobileList(item, USER_ROLE_ADMIN));
                    }
                    List<Notification> allNotifications = Stream.concat(
                            notificationList2.stream(),
                            notificationList.stream()
                    ).collect(Collectors.toList());
                    notificationService.add(allNotifications);
                    //发信息

                    //发微信
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_BOSS));

                }else if(userRole.equals(USER_ROLE_BOSS)){
                    log.info("获取信息成功，上次发给了老板，无需再发");

                }

                notificationCount++;
            }
            else if (minutesPassed >= ALERT_UPGRADE_TIME) {
                log.info("触发管理员警告！");
                //获取上次信息接收者角色
                Integer userRole = notificationSendto(item);
                if(userRole == null){
                    log.info("获取信息失败，直接发给管理员");
                    //TODO:发送信息
                    List<Notification> allNotifications = Stream.concat(
                            buildResponsibilityList(item, USER_ROLE_MEMBER).stream(),
                            buildResponsibilityList(item, USER_ROLE_ADMIN).stream()
                    ).collect(Collectors.toList());

                    notificationService.add(allNotifications);
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_ADMIN));

                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_MEMBER));
                }else if(userRole.equals(USER_ROLE_MEMBER)){
                    log.info("获取信息成功，上次发给了普通员工，升级报警，发送给管理员");
                    List<Notification> notificationList2 = buildResponsibilityList(item, USER_ROLE_MEMBER);
                    if (notificationList2.isEmpty()){
                        log.warn("该项目无成员，无法再发送！");
                    }

                    List<Notification> notificationList = buildResponsibilityList(item,USER_ROLE_ADMIN);
                    if(notificationList.isEmpty()){
                        log.error("获取信息失败!");
                    }
                    //结合起来
                    List<Notification> allNotifications = Stream.concat(
                            notificationList2.stream(),
                            notificationList.stream()
                    ).collect(Collectors.toList());
                    notificationService.add(allNotifications);

                    //发送给成员
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_MEMBER));

                    //发送给管理员
                    wechatAlertUtil.sendAlert(getWebhookUrl(item.getProjectId()),generateAlertMessage(item)
                            , buildMentionedMobileList(item, USER_ROLE_ADMIN));

                }else if(userRole.equals(USER_ROLE_ADMIN)){
                    log.info("获取信息成功，上次发给了管理员，无需再发");
                }
                notificationCount++;
            }else{
                log.info("还在等待项目成员处理~");
            }

        }

        log.info("检查完成，共生成 {} 条通知", notificationCount);
    }

    //查询上一次出现这条信息时发送给了什么角色的人(查不到就要发 查到了就判断是发给谁的 如果四十分钟内发过给管理员就不用发)
    private Integer notificationSendto(Responsibility responsibility) {
        // 查询通知
        log.info("正在查询");
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getErrorId, responsibility.getErrorId())
                .eq(Notification::getProjectId, responsibility.getProjectId())
                .eq(Notification::getErrorType, responsibility.getErrorType())
                .eq(Notification::getPlatform, responsibility.getPlatform())
                .orderByDesc(Notification::getTimestamp)
                .last("limit 1");

        Notification existNotification = notificationMapper.selectOne(queryWrapper);
        log.info("existNotification:{}", existNotification);
        if(existNotification.getContent().equals(ALERT_CONTENT_NEW)){
            return null;
        }
        if (existNotification == null) {
            log.warn("未查询到相关信息！");
            return null;
        }
        //查询被通知者在项目中的角色
        LambdaQueryWrapper<Role> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Role::getUserId, existNotification.getReceiverId())
                .eq(Role::getProjectId, existNotification.getProjectId());

        Role role = roleMapper.selectOne(queryWrapper1);
        if (role == null) {
            log.warn("未查询到相关信息！");
            return null;
        }
        log.info("查询到的角色为：{}", role);
        return role.getUserRole();
    }

        // 查询所有未处理的任务
    private List<Responsibility> findByIsHandle () {
        LambdaQueryWrapper<Responsibility> qw = new LambdaQueryWrapper<>();
        qw.eq(Responsibility::getIsHandle, RepositoryConstants.UN_HANDLED);
        return responsibilityMapper.selectList(qw);

    }

    //构建发送信息的列表
    private List<Notification> buildResponsibilityList (Responsibility item,Integer userRole) {
        List<Notification> result = new ArrayList<>();
        List<Role> roles =new ArrayList<>();
        if (userRole == USER_ROLE_MEMBER) {
            LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
            qw.eq(Role::getUserId, item.getResponsibleId())
                    .eq(Role::getProjectId, item.getProjectId());
            Role role = roleMapper.selectOne(qw);
            roles.add(role);

        } else {
            LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
            qw.eq(Role::getUserRole, userRole).eq(Role::getProjectId, item.getProjectId());
            roles = roleMapper.selectList(qw);
        }
        for (Role role : roles) {
            Notification notification = new Notification();
            notification.setProjectId(item.getProjectId());
            notification.setErrorId(item.getErrorId());
            notification.setResponsibleId(item.getResponsibleId());
            notification.setReceiverId(role.getUserId());
            notification.setPlatform(item.getPlatform());
            notification.setErrorType(item.getErrorType());
            notification.setContent(ALERT_CONTENT_HANDLE);
            result.add(notification);
        }
        return result;
    }
    /**
     * 构建发送微信接收者的手机号码
     */
    protected List<String> buildMentionedMobileList(Responsibility responsibility,Integer userRole) {
        List<Role> roleList = new ArrayList<>();
        List<String> mobileList = new ArrayList<>();

        if (userRole == USER_ROLE_MEMBER) {
            LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
            qw.eq(Role::getUserId, responsibility.getResponsibleId())
                    .eq(Role::getProjectId, responsibility.getProjectId());
            Role role = roleMapper.selectOne(qw);
            roleList.add(role);
        } else {

            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Role::getProjectId, responsibility.getProjectId())
                    .eq(Role::getUserRole, userRole);
            roleList = roleMapper.selectList(queryWrapper);
        }
        for (Role role : roleList) {
            LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Users::getId, role.getUserId());
            Users user = usersMapper.selectOne(queryWrapper1);
            mobileList.add(user.getPhone());


        }
        return mobileList;
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

    /**
     * 发送消息模板
     * @return
     */
    protected String generateAlertMessage(Responsibility item) {
        if(item.getPlatform().equals("mobile"))
        {
            log.info("发送移动端错误！");
            LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MobileError::getId, item.getErrorId());
            MobileError mobileError = mobileErrorMapper.selectOne(queryWrapper);
            return String.format("【移动端错误告警】\n" +
                            "项目ID：%s\n" +
                            "错误类型：%s\n" +
                            "类名：%s\n" +
                            "发生次数：%d\n" +
                            "触发时间：%s\n" +
                            ALERT_CONTENT_HANDLE,
                    mobileError.getProjectId(),
                    mobileError.getErrorType(),
                    mobileError.getClassName(),
                    mobileError.getEvent(),
                    LocalDateTime.now()
                            .format(DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        else if(item.getPlatform().equals("frontend"))
        {
            log.info("发送前端错误！");
            LambdaQueryWrapper<FrontendError> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(FrontendError::getId, item.getErrorId());
            FrontendError frontendError = frontendErrorMapper.selectOne(queryWrapper1);
            return String.format("【前端错误告警】\n" +
                            "项目ID：%s\n" +
                            "错误类型：%s\n" +
                            "错误信息：%s\n" +
                            "发生次数：%d\n" +
                            "触发时间：%s\n" +
                            ALERT_CONTENT_HANDLE,
                    frontendError.getProjectId(),
                    frontendError.getErrorType(),
                    frontendError.getMessage(),
                    frontendError.getEvent(),
                    LocalDateTime.now()
                            .format(DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss")));
        }else if(item.getPlatform().equals("backend")){
            log.info("发送后端错误！");
            LambdaQueryWrapper<BackendError> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(BackendError::getId, item.getErrorId());
            BackendError backendError = backendErrorMapper.selectOne(queryWrapper2);
            return String.format("【后端错误告警】\n" +
                            "项目ID：%s\n" +
                            "错误类型：%s\n" +
                            "堆栈信息：%s\n" +
                            "发生次数：%d\n" +
                            "触发时间：%s\n" +
                            ALERT_CONTENT_HANDLE,
                    backendError.getProjectId(),
                    backendError.getErrorType(),
                    backendError.getStack(),
                    backendError.getEvent(),
                    LocalDateTime.now()
                            .format(DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return null;
    }


}
