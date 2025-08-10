package com.qg.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional
public interface MethodInvocationService {
    void statisticsMethod(Map<String, Integer> methodMap, String projectId);
}
