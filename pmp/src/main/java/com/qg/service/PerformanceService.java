package com.qg.service;

import com.qg.domain.Performance;
import com.qg.domain.Result;

import java.util.List;

public interface PerformanceService {

    Result addPerformance(List<Performance> performance);

    Result selectByProjectId(String projectId);

    Result selectByEnvProjectId(String env, String projectId);


}
