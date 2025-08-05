package com.qg.service;

import com.qg.domain.AlertRule;
import com.qg.domain.Result;

public interface AlertRuleService {
    Result selectByType(String errorType, String env);

    Result updateThreshold(AlertRule alertRule);
}
