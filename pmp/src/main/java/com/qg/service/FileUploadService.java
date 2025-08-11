package com.qg.service;


import com.qg.domain.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
public interface FileUploadService {
    Result uploadFile(String projectId, String timestamp, String version
            , String buildVersion, MultipartFile[] files, String[] jsFilenames, String fileHashes);
}
