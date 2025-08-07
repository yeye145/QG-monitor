package com.qg.controller;

import com.qg.domain.Responsibility;
import com.qg.domain.Result;
import com.qg.service.ResponsibilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "责任链管理")
@RestController
@RequestMapping("/responsibilities")
public class ResponsibilityController {
    @Autowired
   private ResponsibilityService responsibilityService;

    /**
    * 添加责任
    * @param responsibility
    * @return
    */
   @PostMapping
   public Result addResponsibility(Responsibility responsibility)
   {
      return responsibilityService.addResponsibility(responsibility);
   }

    /**
     * 获取项目责任列表
     * @param projectId
     * @return
     */
   @GetMapping
   public Result getResponsibilityList(@RequestParam Long projectId)
   {
      return responsibilityService.getResponsibilityList(projectId);
   }

    /**
     * 获取个人被委派的责任
     * @param responsibleId
     * @return
     */
   @GetMapping("/selectByRespId")
    public Result selectByRespId(@RequestParam Long responsibleId) {
       return responsibilityService.selectByRespId(responsibleId);
   }

    /**
     * 责任人修改
     * @param responsibility
     * @return
     */
   @PutMapping
    public Result updateResponsibility(@RequestBody Responsibility responsibility){
       return responsibilityService.updateResponsibility(responsibility);
   }

    /**
     * 责任删除
     * @param id
     * @return
     */
   @DeleteMapping
    public Result deleteResponsibility(@RequestParam Long id){
       return responsibilityService.deleteResponsibility(id);
   }

}
