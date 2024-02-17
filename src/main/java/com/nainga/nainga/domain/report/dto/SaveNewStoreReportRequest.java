package com.nainga.nainga.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaveNewStoreReportRequest {
    @NotEmpty
    @Schema(defaultValue = "가게 이름", description = "새로 등록하고자 하는 가게 이름")
    private String storeName;   //가게 이름
    @NotEmpty
    @Schema(defaultValue = "주소", description = "새로 등록하고자 하는 가게 주소")
    private String formattedAddress;    //가게 주소
    @NotEmpty
    @Schema(defaultValue = "[\"착한가격업소\", \"모범음식점\", \"안심식당\"]", description = "새로 등록할 가게가 가지고 있는 인증제들의 이름을 담은 리스트. 착한가격업소, 모범음식점, 안심식당이 아닌 경우 예외 발생")
    private List<String> certifications;    //가게가 가지고 있는 인증제 이름 리스트
}