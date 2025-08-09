package com.qg.repository;

import com.qg.domain.BackendLog;
import com.qg.mapper.BackendLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class InfoLogRepository {
    // TODO：目前是10秒缓存过期，6秒定时存一次

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BackendLogMapper backendLogMapper;

    private static final String LOG_KEY = "log:backend:info";
    private static final long TTL_MINUTES = 10;

    private static final ConcurrentHashMap<String, BackendLog>
            infoLogConcurrentHashMap = new ConcurrentHashMap<>();


    /**
     * 统计存储日志
     *
     * @param log
     */
    public void statisticsLog(BackendLog log) {
        // TODO: 生成键（log:backend + project-token + context）
        String redisKey = String.format("%s:%s:%s", LOG_KEY, log.getProjectId(), log.getContext());

        synchronized (redisKey.intern()) {

            // TODO: 检查Redis中，10分钟内是否已存在相同日志
            if (stringRedisTemplate.hasKey(redisKey)) {
                // TODO: 如果该redisKey存在，则递增计数
                stringRedisTemplate.opsForValue().increment(redisKey);
            } else {
                // TODO: redisKey不存在，则插入缓存，并设置TTL为10分钟
                stringRedisTemplate.opsForValue().set(redisKey, "1", TTL_MINUTES, TimeUnit.SECONDS);
            }
            // TODO: 获取或创建BackendInfoLogDTO
            BackendLog backendLog = infoLogConcurrentHashMap.computeIfAbsent(
                    redisKey, k -> log
            );
            backendLog.incrementAndGetEvent();
        }
    }

    /**
     * 将缓存中的数据保存到数据库中
     */
    @Scheduled(fixedRate = 6000)
    public void scheduleSaveToSql() {
        // TODO: 遍历所有缓存的日志
        infoLogConcurrentHashMap.forEach((r, l) -> {
            try {
                synchronized (r.intern()) {
                    stringRedisTemplate.execute(new SessionCallback<>() {
                        @Override
                        public Object execute(RedisOperations operations) throws DataAccessException {
                            operations.watch(r);
                            if (!operations.hasKey(r)) {
                                operations.multi();
                                // TODO: 插入数据库并清除缓存
                                backendLogMapper.insert(l);
                                infoLogConcurrentHashMap.remove(r);
                                operations.exec();
                            } else {
                                operations.unwatch();
                            }
                            return null;
                        }
                    });
                }
            }  catch (DataAccessException e) {
                log.error("插入日志到数据库中失败：{}", e.getMessage());
            }
        });
    }

}
