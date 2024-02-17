package com.nainga.nainga.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveSpecificStoreReportRequest {
    @NotNull
    @Schema(defaultValue = "fix/del", description = "제보 종류를 구분하기 위한 값. fix는 수정 요청, del은 삭제 요청.")
    private String dtype;   //Report 종류를 구분하기 위한 type
    @NotNull
    @Schema(defaultValue = "0", description = "수정 혹은 삭제를 요청하는 가게 id")
    private Long storeId;   //가게 id
    @NotNull
    @Schema(defaultValue = "제보 내용", description = "제보 내용")
    private String contents;    //신고 내용
}
