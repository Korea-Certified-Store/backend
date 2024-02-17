package com.nainga.nainga.domain.report.api;

import com.nainga.nainga.domain.report.application.ReportService;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            "certifications: 가게가 가지고 있는 인증제 리스트들<br>" +
            "[Response Body]<br>" +
            "등록된 reportId<br>")
    @PostMapping("api/report/newStore/v1")
    public Result<Long> saveNewStoreReport(@RequestBody SaveNewStoreReportRequest saveNewStoreReportRequest) {
        Long reportId = reportService.saveNewStoreReport(saveNewStoreReportRequest);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, reportId);
    }
}
