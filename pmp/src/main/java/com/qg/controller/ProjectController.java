package com.qg.controller;

import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name ="项目")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService ProjectService;

    @PostMapping
    public Result addProject(@RequestBody Project project){
        return ProjectService.addProject(project);
    }

}
