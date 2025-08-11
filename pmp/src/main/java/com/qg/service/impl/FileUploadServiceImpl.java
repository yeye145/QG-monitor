package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.service.FileUploadService;
import com.qg.utils.FileUploadHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.qg.utils.FileUploadHandler.determineSubDirectory;
import static com.qg.utils.FileUploadHandler.isValidFile;


@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Data
    @AllArgsConstructor
    private static class FileInfo {
        private String originalName;
        private String storedPath;
        private long size;
        private String fileType;
    }

    /**
     * 上传文件
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
    public Result uploadFile(String projectId, String timestamp, String version
            , String buildVersion, MultipartFile[] files, String[] jsFilenames, String fileHashes) {
        try {

            if (files.length == 0) {
                log.warn("文件上传失败：未接收到任何文件");
                return new Result(Code.BAD_REQUEST, "至少需要上传一个文件");
            }

            // 处理文件上传
            List<FileInfo> uploadedFiles = processFiles(files);

            return new Result(Code.SUCCESS, "文件上传成功", JSONUtil.toJsonStr(Map.of(
                    "projectInfo", Map.of(
                            "projectId", projectId,
                            "version", version
                    ),
                    "fileCount", files.length,
                    "uploadedFiles", uploadedFiles
            )));

        } catch (IOException e) {
            log.error("文件存储失败:{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "文件存储失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("系统处理异常:{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "系统处理异常");
        }
    }

    /**
     * 处理文件存储逻辑
     * @param files
     * @return
     * @throws IOException
     */
    private List<FileInfo> processFiles(MultipartFile[] files) throws IOException {
        List<FileInfo> result = new ArrayList<>();

        for (MultipartFile file : files) {
            // 文件类型校验
            if (!isValidFile(file)) {
                log.warn("跳过无效文件: {}", file.getOriginalFilename());
                continue;
            }

            // 确定存储目录
            String subDir = determineSubDirectory(file);

            // 保存文件
            String fileUrl = FileUploadHandler.saveFile(file, subDir);

            // 记录文件信息
            result.add(new FileInfo(
                    file.getOriginalFilename(),
                    fileUrl,
                    file.getSize(),
                    subDir
            ));

            log.debug("文件保存成功: {} -> {}", file.getOriginalFilename(), fileUrl);
        }

        return result;
    }

}
