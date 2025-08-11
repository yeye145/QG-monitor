package com.qg.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadHandler {

    // 文件存储根目录
    public static final String UPLOAD_DIR = "uploads";
    public static final String IMAGE_DIR = "images";
    public static final String DOCUMENT_DIR = "documents";
    public static final String INSTALL_DIR = "installs";
    public static final String PUBLIC_URL = "http://47.113.224.195:30410";

    // 保存文件到指定目录
    public static String saveFile(MultipartFile file, String subDir) throws IOException {
        // 创建存储目录
        Path uploadPath = Paths.get(UPLOAD_DIR).resolve(subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 保存 绝对路径
        return generatePublicUrl(uniqueFileName, subDir);
    }

    // 验证图片文件类型
    public static boolean isValidImageFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;

        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return fileExtension.matches("\\.(jpg|jpeg|png|gif)$");
    }

    // 验证文档文件类型
    public static boolean isValidDocumentFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;

        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return fileExtension.matches("\\.(pdf|docx|doc)$");
    }

    // 验证安装包类型
    public static boolean isValidInstallFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;

        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return fileExtension.matches("\\.(zip|tar|tar.gz)$");
    }


    // 根据数据库中的相对路径生成完整URL
    public static String generatePublicUrl(String relativePath, String subDir) {
        // 移除相对路径前可能的斜杠（避免重复）
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        // 拼接完整URL
        return PUBLIC_URL + "/" + UPLOAD_DIR + "/" + subDir + "/" + relativePath;
    }
}
