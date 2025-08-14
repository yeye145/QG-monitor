package com.qg.utils;

public class Constants {
    /**
     *  权限标识
     *  0:不可见
     *  1：可读
     *  2：可操作
     */
    public static final Integer PERMISSION_NOT_VISIBLE = 0;
    public static final Integer PERMISSION_READ = 1;
    public static final Integer PERMISSION_OP = 2;

    /**
     * 用户角色
     * 0：老板
     * 1：管理员
     * 2：成员
     */
    public static final Integer USER_ROLE_BOSS = 0;
    public static final Integer USER_ROLE_ADMIN = 1;
    public static final Integer USER_ROLE_MEMBER = 2;

    /**
     * 通知是否已读
     */
    public static final Integer IS_READ = 1;
    public static final Integer IS_NOT_READ = 0;

    /**
     * 通知发送者是否存在
     */
    public static final Integer IS_SENDER_EXIST = 1;
    public static final Integer IS_SENDER_NOT_EXIST = 0;

    /**
     * 错误是否处理
     */
    public static final Integer IS_HANDLE = 1;
    public static final Integer IS_NOT_HANDLE = 0;

}
