package com.nainga.nainga.domain.report.application;

import com.nainga.nainga.domain.report.dao.ReportRepository;
import com.nainga.nainga.domain.report.domain.DelSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.FixSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.NewStoreReport;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.domain.report.dto.SaveSpecificStoreReportRequest;
import com.nainga.nainga.global.exception.GlobalException;
import com.nainga.nainga.global.exception.ReportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public Long saveNewStoreReport(SaveNewStoreReportRequest saveNewStoreReportRequest) {   //사용자의 신규 가게 등록 요청
        NewStoreReport newStoreReport = NewStoreReport.builder()
                .storeName(saveNewStoreReportRequest.getStoreName())
                .formattedAddress(saveNewStoreReportRequest.getFormattedAddress())
                .certifications(saveNewStoreReportRequest.getCertifications())
                .build();

        return reportRepository.save(newStoreReport);
    }

    @Transactional
    public Long saveSpecificStoreReport(SaveSpecificStoreReportRequest saveSpecificStoreReportRequest) throws GlobalException {    //사용자의 특정 가게에 대한 수정, 삭제 요청
        if (saveSpecificStoreReportRequest.getDtype().equals("fix")) {
            FixSpecificStoreReport fixSpecificStoreReport = FixSpecificStoreReport.builder()
                    .storeId(saveSpecificStoreReportRequest.getStoreId())
                    .contents(saveSpecificStoreReportRequest.getContents())
                    .build();

            return reportRepository.save(fixSpecificStoreReport);
        } else if (saveSpecificStoreReportRequest.getDtype().equals("del")) {
            DelSpecificStoreReport delSpecificStoreReport = DelSpecificStoreReport.builder()
                    .storeId(saveSpecificStoreReportRequest.getStoreId())
                    .contents(saveSpecificStoreReportRequest.getContents())
                    .build();

            return reportRepository.save(delSpecificStoreReport);
        } else {
            throw new GlobalException(ReportErrorCode.INVALID_DTYPE);   //잘못된 DTYPE이 들어왔을 경우에 Custom GlobalException 처리
        }
    }
}
