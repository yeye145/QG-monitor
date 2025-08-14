package com.qg.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourcemapFiles {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String projectId;
    private LocalDateTime timestamp;
    private String version;
    private String buildVersion;
    private String fileName;
    private String filePath;
    private String fileHash;
    private String jsFilename;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
