package com.nainga.nainga.domain.report.api;

import com.nainga.nainga.domain.report.application.ReportService;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.domain.report.dto.SaveSpecificStoreReportRequest;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportApi {
    private final ReportService reportService;

    //사용자의 신규 가게 등록 요청에 대한 제보를 저장
    @Tag(name = "[New] 사용자 제보")
    @Operation(summary = "사용자의 신규 가게 등록 요청에 대한 제보를 서버에 저장", description = "사용자의 신규 가게 등록 요청에 대한 제보를 서버에 저장합니다.<br><br>" +
            "[Request Body]<br>" +
            "storeName: 등록 요청하는 가게 이름<br>" +
            "formattedAddress: 등록 요청하는 가게 주소<br>" +
            "certifications: 가게가 가지고 있는 인증제들의 이름을 담은 리스트. 착한가격업소, 모범음식점, 안심식당이 아닌 경우 예외 발생<br>" +
            "[Response Body]<br>" +
            "등록된 reportId<br>")
    @PostMapping("api/report/newStore/v1")
    public Result<Long> saveNewStoreReport(@Valid @RequestBody SaveNewStoreReportRequest saveNewStoreReportRequest) {
        Long reportId = reportService.saveNewStoreReport(saveNewStoreReportRequest);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, reportId);
    }

    //사용자의 특정 가게에 대한 수정, 삭제 요청 정보를 저장
    @Tag(name = "[New] 사용자 제보")
    @Operation(summary = "사용자의 특정 가게에 대한 정보 수정 혹은 삭제 요청에 대한 제보를 서버에 저장", description = "사용자의 특정 가게에 대한 정보 수정 혹은 삭제 요청에 대한 제보를 서버에 저장합니다.<br><br>" +
            "[Request Body]<br>" +
            "dtype: 제보 종류를 구분하기 위한 값. fix는 수정 요청, del은 삭제 요청. fix나 del이 아닌 경우 예외 발생<br>" +
            "storeId: 수정 혹은 삭제를 요청하는 가게 id<br>" +
            "contents: 제보 내용<br>" +
            "[Response Body]<br>" +
            "등록된 reportId<br>")
    @PostMapping("api/report/specificStore/v1")
    public Result<Long> saveSpecificStoreReport(@Valid @RequestBody SaveSpecificStoreReportRequest saveSpecificStoreReportRequest) {
        Long reportId = reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, reportId);
    }
}
