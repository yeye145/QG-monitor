package com.qg.service.impl;

import com.qg.repository.MethodInvocationRepository;
import com.qg.service.MethodInvocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MethodInvocationServiceImpl implements MethodInvocationService {

    private final MethodInvocationRepository methodInvocationRepository;

    @Autowired
    public MethodInvocationServiceImpl(MethodInvocationRepository methodInvocationRepository) {
        this.methodInvocationRepository = methodInvocationRepository;
    }

    @Override
    public void statisticsMethod(Map<String, Integer> methodMap, String projectId) {
        try {
            Map<String, Integer> processedMap = methodMap.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toMap(
                            entry -> projectId + ":" + entry.getKey(),
                            Map.Entry::getValue
                    ));

            methodInvocationRepository.statisticsMethod(processedMap);
            log.info("成功统计{}个方法的调用情况,项目ID: {}", processedMap.size(), projectId);
        } catch (Exception e) {
            log.error("方法调用统计失败,项目ID: {}: {}", projectId, e.getMessage(), e);
        }
    }
}