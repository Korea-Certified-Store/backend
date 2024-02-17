package com.nainga.nainga.domain.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveSpecificStoreReportRequest {
    @NotNull
    private String dtype;   //Report 종류를 구분하기 위한 type
    @NotNull
    private Long storeId;   //가게 id
    @NotNull
    private String contents;    //신고 내용
}
