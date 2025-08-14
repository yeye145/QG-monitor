package com.qg.service;

import com.qg.domain.Result;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public interface SourcemapFilesService {
    Result uploadFile(String projectId, String timestamp, String version, String buildVersion, MultipartFile[] files, String[] jsFilenames, String fileHashes);
}
