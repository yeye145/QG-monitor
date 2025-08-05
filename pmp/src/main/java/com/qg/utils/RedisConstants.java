package com.qg.utils;

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
}
