package com.qg.utils;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class RedisConstants {
    /**
     * 验证码
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;

    /**
     * token验证
     * tokenkey的头
     * token有效期
     */
    public static final String LOGIN_USER_KEY = "login:user:";
    public static final long LOGIN_USER_TTL = 30L;

    /**
     * 错误 重复
     */
    public static final String ERROR_REPEAT_KEY = "error:repeat:";
    public static final Integer MAX_ERROR_TIME = 10;

    /**
     * 邀请码
     */
    public static final String INVITE_CODE_KEY = "inviteCode:";
    public static final Long INVITE_CODE_TTL = 10L;


}
