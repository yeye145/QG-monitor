package com.qg.service;

import com.qg.domain.Error;
import com.qg.domain.Result;

import java.util.List;

public interface ErrorService {
    Result addError(List<Error> errorList);

    Result selectByEnv(String env, String projectId, Long moduleId);
}
