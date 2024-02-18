package com.nainga.nainga.domain.report.api;

import com.nainga.nainga.domain.report.application.ReportService;
import com.nainga.nainga.domain.report.domain.Report;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.domain.report.dto.SaveSpecificStoreReportRequest;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    //reportId를 가지고 사용자 제보 내용 조회
    @Tag(name = "[New] 사용자 제보")
    @Operation(summary = "reportId를 가지고 사용자 제보 내용 조회", description = "reportId를 가지고 사용자 제보 내용을 조회합니다.<br><br>" +
            "[Request Body]<br>" +
            "reportId: 검색할 사용자 제보의 reportId. 유효하지 않은 reportId의 경우 예외 발생<br>" +
            "[Response Body]<br>" +
            "해당 reportId로 검색된 사용자 제보 내용<br>")
    @GetMapping("api/report/byId/v1")
    public Result<Report> findById(@NotNull @RequestParam(value = "reportId") Long reportId) {
        Report report = reportService.findById(reportId);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, report);
    }

    //DB에 있는 모든 사용자 제보 내용 조회
    @Tag(name = "[New] 사용자 제보")
    @Operation(summary = "DB에 있는 모든 사용자 제보 내용 조회", description = "DB에 있는 모든 사용자 제보 내용을 조회합니다.<br><br>" +
            "[Response Body]<br>" +
            "DB에 있는 모든 사용자 제보 내용<br>")
    @GetMapping("api/report/all/v1")
    public Result<List<Report>> findAll() {
        List<Report> reports = reportService.findAll();
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, reports);
    }
}
