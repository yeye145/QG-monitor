package com.qg.controller;

import com.qg.domain.AlertRule;
import com.qg.domain.Result;
import com.qg.service.AlertRuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name ="告警规则")
@RestController
@RequestMapping("/alertRules")
public class AlertRuleController {

    @Autowired
    private AlertRuleService alertRuleService;

    /**
     * 根据错误类型查询报警规则
     * @param errorType
     * @return
     */
    @GetMapping("/selectByTypeEnvProjectId")
    public Result selectByType(@RequestParam String errorType, @RequestParam(required = false) String env,
                               @RequestParam String projectId, @RequestParam String platform) {
        return alertRuleService.selectByType(errorType, env, projectId, platform);
    }

    /**
     * 修改报警阈值（添加与修改）
     * @param alertRule
     * @return
     */
    @PutMapping("/updateThreshold")
    public Result updateThreshold(@RequestBody AlertRule alertRule) {
        return alertRuleService.updateThreshold(alertRule);
    }


}
