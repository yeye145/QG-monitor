package com.qg.service;

import com.qg.domain.Module;
import com.qg.domain.Result;

public interface ModuleService {
    Result addModule(Module module);

    Result selectByProjectId(String projectId);

    Result deleteById(Long id);

    void putModuleIfAbsent(String moduleName, String projectId);
}
