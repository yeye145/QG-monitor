package com.qg.controller;

import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name ="项目")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService ProjectService;

    /**
     * 创建项目
     * @param project
     * @return
     */
    @PostMapping
    public Result addProject(@RequestBody Project project){
        return ProjectService.addProject(project);
    }

    /**
     * 更新项目信息
     * @param project
     * @return
     */
    @PutMapping("/update")
    public Result updateProject(@RequestBody Project project){
        return ProjectService.updateProject(project);
    }

    /**
     * 删除项目
     * @param uuid
     * @return
     */
    @DeleteMapping
    public Result deleteProject(@RequestParam String uuid){
        return ProjectService.deleteProject(uuid);
    }

    /**
     * 获取项目详细信息
     * @param uuid
     * @return
     */
    @GetMapping("/getProject")
    public Result getProjectList(@RequestParam String uuid){
        return ProjectService.getProject(uuid);
    }

    /**
     * 公开项目查看列表
     * @return
     */
    @GetMapping("/getProjectList")
    public Result getProjectList(){
        return ProjectService.getProjectList();
    }

    /**
     * 获取个人公开项目列表
     * @param userId
     * @return
     */
    @GetMapping("/getPersonalPublicProject")
    public Result getPersonalPublicProject(@RequestParam Long userId){
        return ProjectService.getPersonalPublicProject(userId);
    }

    /**
     * 获取个人非公开项目列表
     * @param userId
     * @return
     */
    @GetMapping("/getPersonalUnpublicProject")
    public Result getPersonalUnpublicProject(@RequestParam Long userId){
        return ProjectService.getPersonalUnpublicProject(userId);
    }

}
