package com.qg.repository;

import com.qg.domain.BackendLog;
import com.qg.mapper.BackendLogMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;


@Repository
public class BackendLogRepository extends StatisticsDataRepository<BackendLog> {
    private static final String KEY_PREFIX = "backend:log";
    private static final long TTL_MINUTES = 1;

    @Autowired
    private BackendLogMapper backendLogMapper;

    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES;
    }

    @Override
    protected void saveToDatabase(BackendLog log) {
        backendLogMapper.insert(log);
    }

    @Override
    protected String generateUniqueKey(BackendLog log) {
        return String.format("%s-%s:%s-%s:%s",
                KEY_PREFIX, log.getLevel(),
                log.getProjectId(), log.getEnvironment(), log.getContext()
        );
    }

    @Override
    protected void incrementEvent(BackendLog log) {
        log.incrementEvent();
    }
}