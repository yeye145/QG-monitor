package com.qg.service.impl;

import com.qg.domain.Code;
import com.qg.domain.Error;
import com.qg.domain.Result;
import com.qg.mapper.ErrorMapper;
import com.qg.service.ErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;

@Slf4j
@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorMapper errorMapper;

    @Override
    public Result addError(List<Error> errorList) {
        log.debug("添加错误信息: {}", errorList);
        if (errorList == null || errorList.isEmpty()) {
            log.error("添加错误信息失败，错误信息为空");
            return new Result(Code.BAD_REQUEST, "添加错误信息失败，错误信息为空");
        }

        try {
            log.debug("开始批量保存，数据量: {}", errorList.size());
            // 使用注入的 mapper
            for (Error error : errorList) {
                errorMapper.insert(error);
            }
            log.info("添加错误信息成功，共处理 {} 条记录", errorList.size());
            return new Result(Code.SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息失败，错误信息: {}", errorList, e);
            return new Result(Code.INTERNAL_ERROR, "添加错误信息失败: " + e.getMessage());
        }
    }
}
