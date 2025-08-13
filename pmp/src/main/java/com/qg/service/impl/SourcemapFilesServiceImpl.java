package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.domain.SourcemapFiles;
import com.qg.mapper.ProjectMapper;
import com.qg.mapper.SourcemapFilesMapper;
import com.qg.service.ProjectService;
import com.qg.service.SourcemapFilesService;
import com.qg.utils.FileUploadHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.qg.utils.FileUploadHandler.*;

@Service
@Slf4j
public class SourcemapFilesServiceImpl implements SourcemapFilesService {

    @Autowired
    private SourcemapFilesMapper sourcemapFilesMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 上传文件
     *
     * @param projectId
     * @param timestamp
     * @param version
     * @param buildVersion
     * @param files
     * @param jsFilenames
     * @param fileHashes
     * @return
     */
    @Override
    public Result uploadFile(String projectId, String timestamp, String version,
                             String buildVersion, MultipartFile[] files, String[] jsFilenames, String fileHashes) {

        // 参数校验
        if (files == null || files.length == 0) {
            log.warn("文件上传失败：未接收到任何文件");
            return new Result(Code.BAD_REQUEST, "至少需要上传一个文件");
        }

        if (projectId == null || projectId.isEmpty() || jsFilenames == null || files.length != jsFilenames.length) {
            log.warn("文件上传失败：参数错误");
            return new Result(Code.BAD_REQUEST, "参数错误");
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (timestamp != null && !timestamp.isEmpty()) {
            try {
                // 将毫秒时间戳转换为 LocalDateTime
                long timestampMillis = Long.parseLong(timestamp);
                localDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis),
                        ZoneId.systemDefault() // 使用系统默认时区
                );
            } catch (NumberFormatException e) {
                log.warn("时间戳格式不正确，无法解析为数字: {}", timestamp);
                return new Result(Code.BAD_REQUEST, "时间戳格式不正确");
            } catch (Exception e) {
                log.warn("时间戳转换失败: {}", timestamp);
                return new Result(Code.BAD_REQUEST, "时间戳转换失败");
            }
        }

        try {
            // 判断项目是否存在
            LambdaQueryWrapper<Project> projectQueryWrapper = new LambdaQueryWrapper<>();
            projectQueryWrapper.eq(Project::getUuid, projectId);
            Project project = projectMapper.selectOne(projectQueryWrapper);
            if (project == null) {
                return new Result(Code.NOT_FOUND, "项目不存在");
            }

            List<SourcemapFiles> savedFiles = new ArrayList<>();

            // 处理每个文件
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String jsFilename = jsFilenames[i];

                if (file == null || !isValidMapFile(file)) {
                    log.info("跳过无效文件: {}", file != null ? file.getOriginalFilename() : "null");
                    continue;
                }

                String filePath = saveFile(file, MAP_DIR);
                String fileName = file.getOriginalFilename();

                SourcemapFiles sourcemapFiles = new SourcemapFiles();
                sourcemapFiles.setProjectId(projectId);

                sourcemapFiles.setTimestamp(localDateTime);

                sourcemapFiles.setVersion(version);
                sourcemapFiles.setBuildVersion(buildVersion);
                sourcemapFiles.setFileName(fileName);
                sourcemapFiles.setFilePath(filePath);
                sourcemapFiles.setFileHash(fileHashes);
                sourcemapFiles.setJsFilename(jsFilename);

                sourcemapFilesMapper.insert(sourcemapFiles);
                savedFiles.add(sourcemapFiles);
            }

            if (savedFiles.isEmpty()) {
                log.warn("没有有效文件被保存");
                return new Result(Code.BAD_REQUEST, "没有有效文件被保存");
            }

            log.info("成功上传 {} 个map文件到项目 {}", savedFiles.size(), projectId);
            return new Result(Code.SUCCESS, "文件上传成功");

        } catch (IOException e) {
            log.error("map文件存储失败", e);
            return new Result(Code.INTERNAL_ERROR, "文件存储失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("系统处理异常", e);
            return new Result(Code.INTERNAL_ERROR, "系统处理异常: " + e.getMessage());
        }
    }

}
