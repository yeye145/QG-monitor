package com.qg.repository;

import com.qg.domain.MobileError;
import com.qg.mapper.MobileErrorMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;


@Repository
public class MobileErrorRepository extends StatisticsDataRepository<MobileError> {
    private static final String KEY_PREFIX = "mobile:error";
    private static final long TTL_MINUTES = 1;

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES;
    }

    @Override
    protected void saveToDatabase(MobileError error) {
        mobileErrorMapper.insert(error);
    }

    @Override
    protected String generateUniqueKey(MobileError error) {
        return String.format("%s:%s:%s:%s",
                KEY_PREFIX,
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName()
        );
    }

    @Override
    protected void incrementEvent(MobileError error) {
        error.incrementEvent();
    }
}