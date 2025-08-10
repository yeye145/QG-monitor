package com.qg.controller;

import cn.hutool.json.JSONUtil;
import com.qg.domain.*;
import com.qg.dto.FileUploadDTO;
import com.qg.service.FrontendBehaviorService;
import com.qg.service.FrontendErrorService;
import com.qg.service.FrontendPerformanceService;
import com.qg.utils.FileUploadHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.qg.domain.Code.INTERNAL_ERROR;
import static com.qg.utils.FileUploadHandler.isValidFile;

/**
 * @Description: 前端业务类  // 类说明
 * @ClassName: FrontendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:59   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@RequestMapping("/frontend")
@RestController
public class FrontendController {

    @Autowired
    private FrontendPerformanceService frontendPerformanceService;

    @Autowired
    private FrontendErrorService frontendErrorService;

    @Autowired
    private FrontendBehaviorService frontendBehaviorService;


//    @PostMapping("/performance")
//    public void getPerformanceData(@RequestBody String performanceData) {
//        log.info("***********接收到了前端性能数据***********");
//        log.info(performanceData);
//        List<FrontendPerformance> frontendPerformance = JSONUtil.toList(performanceData, FrontendPerformance.class);
//        if (frontendPerformanceService.saveFrontendPerformance(frontendPerformance) > 0) {
//            log.info("已接收的前端性能数据: " + frontendPerformance);
//        } else {
//            log.error("接收前端性能数据失败");
//        }
//    }
//
//    @PostMapping("/error")
//    public Result getErrorData(@RequestBody String errorData) {
//        log.info("***********接收到了前端错误数据***********");
//        log.info(errorData);
//        return frontendErrorService.addFrontendError(errorData);
//    }
//
//    @PostMapping("/behavior")
//    public void getBehaviorData(@RequestBody String behaviorData) {
//        log.info("***********接收到了前端行为数据***********");
//        log.info(behaviorData);
//        List<FrontendBehavior> behaviorList = JSONUtil.toList(behaviorData, FrontendBehavior.class);
//        if (frontendBehaviorService.saveFrontendBehavior(behaviorList) > 0) {
//            log.info("已接收的前端行为数据: " + behaviorList);
//        } else {
//            log.error("接收前端行为数据失败");
//        }
//    }

    @PostMapping("/{type}")
    public Result getData(@RequestBody String data,@PathVariable String type) {

        switch (type) {
            case "performance":
                log.info("************接收到前端性能数据*************");
                log.info("前端性能数据: " + data);
                return frontendPerformanceService.saveFrontendPerformance(data);
            case "error":
                log.info("************接收到前端错误数据*************");
                log.info("前端错误数据: " + data);
                return frontendErrorService.addFrontendError(data);
            case "behavior":
                log.info("************接收到前端用户行为数据*************");
                log.info("前端用户行为数据: " + data);
                return frontendBehaviorService.saveFrontendBehavior(data);
            default:
                log.error("未知的数据类型: " + type);
                return new Result(INTERNAL_ERROR, "未知的数据类型");
        }

    }

    @PostMapping("/formData")
    public Result getFile(@RequestParam String projectId, @RequestParam String timestamp, @RequestParam String version,
                        @RequestParam String buildVersion, @RequestParam MultipartFile[] files,
                        @RequestParam String [] jsFilenames, @RequestParam String fileHashes) {

        log.info("\n项目ID: " + projectId +
                "\n时间戳: " + timestamp +
                "\n版本: " + version +
                "\n构建版本: " + buildVersion +
                "\n文件数量: " + files.length +
                "\nJS文件名: " + String.join(", ", jsFilenames) +
                "\n文件哈希: " + fileHashes);

        try {
            // 参数校验
            if (files.length == 0) {
                log.warn("文件上传失败：未接收到任何文件");
                return new Result(Code.BAD_REQUEST, "至少需要上传一个文件");
            }

            // 构建DTO
            FileUploadDTO dto = new FileUploadDTO();
            dto.setProjectId(projectId);
            dto.setTimestamp(timestamp);
            dto.setVersion(version);
            dto.setBuildVersion(buildVersion);
            dto.setJsFilenames(jsFilenames);
            dto.setFileHashes(fileHashes);

            // 处理文件上传
            List<FileInfo> uploadedFiles = processFiles(files, dto);

            // 返回结构化响应
//            return new Result(Code.SUCCESS, "文件上传成功", new Map());

            return null;
        } catch (IOException e) {
            log.error("文件存储失败{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "文件存储失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("系统处理异常{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "系统处理异常");
        }
    }

    /**
     * 处理文件存储逻辑
     */
    private List<FileInfo> processFiles(MultipartFile[] files, FileUploadDTO dto) throws IOException {
        List<FileInfo> result = new ArrayList<>();

        for (MultipartFile file : files) {
            // 1. 文件类型校验
            if (!isValidFile(file)) {
                log.warn("跳过无效文件: {}", file.getOriginalFilename());
                continue;
            }

            // 2. 确定存储目录
            String subDir = determineSubDirectory(file);

            // 3. 保存文件
            String fileUrl = FileUploadHandler.saveFile(file, subDir);

            // 4. 记录文件信息
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



    /**
     * 根据文件类型确定存储目录
     */
    private String determineSubDirectory(MultipartFile file) {
        if (FileUploadHandler.isValidImageFile(file)) {
            return FileUploadHandler.IMAGE_DIR;
        } else if (FileUploadHandler.isValidDocumentFile(file)) {
            return FileUploadHandler.DOCUMENT_DIR;
        } else {
            return FileUploadHandler.INSTALL_DIR;
        }
    }

    @Data
    @AllArgsConstructor
    private static class FileInfo {
        private String originalName;
        private String storedPath;
        private long size;
        private String fileType;
    }


}
