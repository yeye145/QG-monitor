package com.qg.repository;

import com.qg.domain.BackendLog;
import com.qg.mapper.BackendLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;

import static com.qg.repository.RepositoryConstants.BACKEND_LOG_PREFIX;
import static com.qg.repository.RepositoryConstants.TTL_MINUTES;

@Slf4j
@Repository
public class BackendLogRepository extends StatisticsDataRepository<BackendLog> {

    @Autowired
    private BackendLogMapper backendLogMapper;

    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES.getAsLong();
    }

    @Override
    protected void saveToDatabase(BackendLog backendLog) {
        try {
            backendLogMapper.insert(backendLog);
        } catch (Exception e) {
            log.error("保存日志到数据库时出现异常:{}", e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(BackendLog log) {
        return String.format("%s-%s:%s-%s:%s",
                BACKEND_LOG_PREFIX.getAsString(), log.getLevel(),
                log.getProjectId(), log.getEnvironment(), log.getContext()
        );
    }

    @Override
    protected void incrementEvent(BackendLog log) {
        log.incrementEvent();
    }
}