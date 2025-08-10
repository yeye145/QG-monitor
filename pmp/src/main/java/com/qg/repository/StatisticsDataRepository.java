package com.qg.repository;

import com.qg.domain.MobileError;
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

@Repository
@Slf4j
public abstract class StatisticsDataRepository<T> {
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    protected final ConcurrentHashMap<String, T> cacheMap = new ConcurrentHashMap<>();

    protected abstract long getTtlMinutes();
    protected abstract void saveToDatabase(T entity);
    protected abstract String generateUniqueKey(T entity);
    protected abstract void incrementEvent(T entity);

    /**
     * 统计并缓存数据
     */
    public void statistics(T entity) {
        // 获取前缀
        String key = generateUniqueKey(entity);

        synchronized (key.intern()) {
            // Redis计数，如果存在则加一，不存在则新建
            if (stringRedisTemplate.hasKey(key)) {
                stringRedisTemplate.opsForValue().increment(key);
            } else {
                stringRedisTemplate.opsForValue().set(
                        key, "1", getTtlMinutes(), TimeUnit.MINUTES
                );
            }

            // 更新内存缓存
            T cached = cacheMap.computeIfAbsent(key, k -> entity);
            incrementEvent(cached);

            // 如果是MobileError类型，触发告警检查
            if (entity instanceof MobileError) {
                MobileErrorFatherRepository repository = (MobileErrorFatherRepository) this;
                repository.sendWechatAlert((MobileError) cached);
            }
        }
    }

    /**
     * 定时将缓存中的数据批量存入数据库
     */
    @Scheduled(fixedRate = 6000)
    public void scheduleSaveToDatabase() {
        cacheMap.forEach((key, entity) -> {
            try {
                synchronized (key.intern()) {
                    stringRedisTemplate.execute(new SessionCallback<>() {
                        @Override
                        public Object execute(RedisOperations operations) throws DataAccessException {
                            operations.watch(key);
                            if (!operations.hasKey(key)) {
                                operations.multi();

                                // 保存到数据库，移除本地缓存
                                saveToDatabase(entity);
                                cacheMap.remove(key);
                                operations.exec();
                            } else {
                                operations.unwatch();
                            }
                            return null;
                        }
                    });
                }
            } catch (DataAccessException e) {
                log.error("插入数据到数据库失败：{}", e.getMessage());
            }
        });
    }

}