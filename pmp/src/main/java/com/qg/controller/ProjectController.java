package com.qg.controller;

import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.dto.InviteDto;
import com.qg.service.ProjectService;
import com.qg.vo.PersonalProjectVO;
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
     * @param personalProjectVO
     * @return
     */
    @PostMapping
    public Result addProject(@RequestBody PersonalProjectVO personalProjectVO){
        return ProjectService.addProject(personalProjectVO);
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
     * 获取公开项目
     * @return
     */
    @GetMapping("/getPublicProjectList")
    public Result getPublicProjectList(){
        return ProjectService.getPublicProjectList();
    }

    /**
     * 获取私有项目
     * @return
     */
    @GetMapping("/getPrivateProject")
    public Result getPrivateProjectList() {
        return ProjectService.getPrivateProjectList();
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

    /**
     * 获取项目邀请码
     * @param projectId
     * @return
     */
    @GetMapping("/getInviteCode")
    public Result getInviteCode(@RequestParam String projectId){
        return ProjectService.getInviteDCode(projectId);
    }

    /**
     * 加入项目
     * @param inviteDto
     * @return
     */
    @PostMapping("/joinProject")
    public Result joinProject(@RequestBody InviteDto inviteDto){
        return ProjectService.joinProject(inviteDto);
    }

    /**
     * 根据名称查询项目
     * @param name
     * @return
     */
    @GetMapping("/selectProjectByName")
    public Result selectProjectByName(@RequestParam String name){
        return ProjectService.selectProjectByName(name);
    }
}
