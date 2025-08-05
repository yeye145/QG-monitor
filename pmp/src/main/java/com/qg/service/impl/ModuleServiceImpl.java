package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.Code;
import com.qg.domain.Module;
import com.qg.domain.Result;
import com.qg.mapper.ModuleMapper;
import com.qg.service.ModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleMapper moduleMapper;

    @Override
    public Result addModule(Module module) {
        log.info("添加模块: {}", module);
        if (module == null) {
            log.error("添加模块失败，模块参数为空");
            return new Result(Code.BAD_REQUEST, "添加模块失败，模块参数为空");
        }
        if (module.getProjectId() == null || module.getProjectId().isEmpty()
                || module.getModuleName() == null || module.getModuleName().isEmpty()) {
            log.error("添加模块失败，模块参数不完整");
            return new Result(Code.BAD_REQUEST, "添加模块失败，模块参数不完整");
        }

        // 判断项目id是否存在


        try {
            boolean result = moduleMapper.insert(module) > 0;
            if (result) {
                log.info("添加模块成功: {}", module);
                return new Result(Code.SUCCESS, "添加模块成功");
            } else {
                log.error("添加模块失败，模块参数: {}", module);
                return new Result(Code.INTERNAL_ERROR, "添加模块失败");
            }
        } catch (Exception e) {
            log.error("添加模块失败，模块参数: {}", module, e);
            return new Result(Code.INTERNAL_ERROR, "添加模块失败: " + e.getMessage());
        }
    }

    @Override
    public Result selectByProjectId(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            log.error("查询模块失败，参数为空");
            return new Result(Code.BAD_REQUEST, "查询模块失败，参数为空");
        }
        try {
            LambdaQueryWrapper<Module> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Module::getProjectId, projectId.trim())
                    .isNull(Module::getDeletedTime);
            List<Module> moduleList = moduleMapper.selectList(queryWrapper);
            log.info("成功查询模块: {}", moduleList);
            return new Result(Code.SUCCESS, moduleList, "查询成功");
        } catch (Exception e) {
            log.error("查询模块失败，参数: {}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询模块失败: " + e.getMessage());
        }
    }

    @Override
    public Result deleteById(Long id) {
        if (id == null) {
            log.error("删除模块失败，参数为空");
            return new Result(Code.BAD_REQUEST, "删除模块失败，参数为空");
        }

        try {
            // 创建更新包装器
            LambdaUpdateWrapper<Module> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Module::getId, id)
                    .set(Module::getDeletedTime, LocalDateTime.now());

            // 执行更新操作
            int result = moduleMapper.update(null, updateWrapper);
            if (result > 0) {
                log.info("删除模块成功，id: {}", id);
                return new Result(Code.SUCCESS, "删除成功");
            } else {
                log.warn("删除模块失败，id: {}", id);
                return new Result(Code.NOT_FOUND, "删除失败，模块不存在");
            }
        } catch (Exception e) {
            log.error("删除模块失败，模块ID: {}", id, e);
            return new Result(Code.INTERNAL_ERROR, "删除模块失败: " + e.getMessage());
        }
    }
}
