package com.qg.repository;

/**
 * 仓库层公共常量
 */
public enum RepositoryConstants {
    // 默认阈值
    DEFAULT_THRESHOLD(10),

    // RedisKey前缀
    BACKEND_LOG_PREFIX("backend:log"),
    MOBILE_ERROR_PREFIX("mobile:error"),
    REDIS_KEY_PREFIX("backend:method:"),


    // TTL时间（分钟）
    TTL_MINUTES(1),

    // 企业微信字段
    MESSAGE_TYPE("msgtype"),
    TEXT("text"),
    CONTENT("content"),
    MENTIONED_MOBILE_LIST("mentioned_mobile_list");

    // 定时任务配置
    public static final int FIXED_RATE_DEFAULT = 6000;
    public static final int FIXED_RATE_METHOD = 12000;


    private final Object value;

    RepositoryConstants(Object value) {
        this.value = value;
    }

    public <T> T getValue() {
        @SuppressWarnings("unchecked")
        T result = (T) value;
        return result;
    }

    // 类型安全的获取方法
    public Integer getAsInt() {
        return (Integer) value;
    }

    public String getAsString() {
        return (String) value;
    }

    public Long getAsLong() {
        return ((Integer) value).longValue();
    }
}