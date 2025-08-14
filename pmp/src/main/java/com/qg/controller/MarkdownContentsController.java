package com.qg.controller;

import com.qg.domain.Result;
import com.qg.service.MarkdownContentsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SDK说明文档")
@RequestMapping("/markdownContents")
public class MarkdownContentsController {

    @Autowired
    private MarkdownContentsService markdownContentsService;


    @GetMapping("/select")
    public Result select() {
        return markdownContentsService.select();
    }
}
