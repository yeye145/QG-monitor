package com.qg.service;

import com.qg.domain.Responsibility;
import com.qg.domain.Result;

public interface ResponsibilityService {
    Result addResponsibility(Responsibility responsibility);

    Result getResponsibilityList(String projectId);

    Result selectByRespId(Long responsibleId);

    Result updateResponsibility(Responsibility responsibility);

    Result deleteResponsibility(Long id);

    Result selectResponsibleError(String projectId, Long responsibleId, String errorType, String platform);

    Result updateHandleStatus(Responsibility responsibility);

    Result selectHandleStatus(String projectId, String errorType, String platform);
}
