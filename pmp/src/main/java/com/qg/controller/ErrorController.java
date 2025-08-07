package com.qg.controller;

import com.qg.domain.Error;
import com.qg.domain.Result;
import com.qg.service.ErrorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "错误信息")
@RestController
@RequestMapping("/errors")
public class ErrorController {

    @Autowired
    private ErrorService errorService;

    /**
     * 添加错误信息
     * @param errorList 错误信息列表
     * @return 添加结果
     */
    @PostMapping("/addError")
    public Result addError(@RequestBody List<Error> errorList) {
        return errorService.addError(errorList);
    }

    /**
     * 根据四条件查询错误信息
     * @param env
     * @return
     */
    @GetMapping("/selectByCondition")
    public Result selectByCondition(@RequestParam String env, @RequestParam String projectId,
                                    @RequestParam(required = false) Long moduleId, @RequestParam(required = false) String type,
                                    @RequestParam(required = false) String platform) {
        return errorService.selectByEnvProjectModule(env, projectId, moduleId, type, platform);
    }

    /**
     * 根据id查询错误信息
     * @param id
     * @return
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        return errorService.selectById(id);
    }
}
