package com.qg.vo;

import com.qg.domain.FrontendError;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FrontendErrorVO {
    private String projectId;
    private LocalDateTime timestamp;

    private List<FrontendError> data;
}
