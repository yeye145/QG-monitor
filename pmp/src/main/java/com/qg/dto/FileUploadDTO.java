package com.qg.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadDTO {
    private String projectId;
    private String timestamp;
    private String version;
    private String buildVersion;
    private MultipartFile[] files;
    private String[] jsFilenames;
    private String fileHashes;
}
