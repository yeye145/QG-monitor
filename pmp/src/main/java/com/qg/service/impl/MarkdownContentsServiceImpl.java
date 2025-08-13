package com.qg.service.impl;

import com.qg.domain.Code;
import com.qg.domain.MarkdownContents;
import com.qg.domain.Result;
import com.qg.mapper.MarkdownContentsMapper;
import com.qg.service.MarkdownContentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qg.domain.Code.SUCCESS;

@Service
@Slf4j
public class MarkdownContentsServiceImpl implements MarkdownContentsService {

    @Autowired
    private MarkdownContentsMapper markdownContentsMapper;

    @Override
    public Result select() {
        List<MarkdownContents> markdownContents = markdownContentsMapper.selectList(null);
        log.info("md文件查询成功");

        return new Result(SUCCESS, markdownContents, "md文件查询成功");
    }
}
