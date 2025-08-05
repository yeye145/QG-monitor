package com.qg.service;

import com.qg.domain.AlertRule;
import com.qg.domain.Result;

public interface AlertRuleService {
    Result selectByType(String errorType);

    Result updateThreshold(AlertRule alertRule);
}
