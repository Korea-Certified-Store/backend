package com.nainga.nainga.domain.report.application;

import com.nainga.nainga.domain.report.dao.ReportRepository;
import com.nainga.nainga.domain.report.domain.DelSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.FixSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.NewStoreReport;
import com.nainga.nainga.domain.report.domain.Report;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.domain.report.dto.SaveSpecificStoreReportRequest;
import com.nainga.nainga.global.exception.GlobalException;
import com.nainga.nainga.global.exception.ReportErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public Long saveNewStoreReport(SaveNewStoreReportRequest saveNewStoreReportRequest) throws GlobalException {   //사용자의 신규 가게 등록 요청
        List<String> certificationList = List.of("착한가격업소", "모범음식점", "안심식당");    //현재 App에서 사용중인 Certification 목록
        for (String certification : saveNewStoreReportRequest.getCertifications()) {
            if (!certificationList.contains(certification)) {
                throw new GlobalException(ReportErrorCode.INVALID_CERTIFICATION);   //잘못된 인증제 값이 들어온 것이므로 예외 발생
            }
        }

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

    public Report findById(Long id) throws GlobalException {    //reportId를 가지고 report를 DB에서 조회하는 로직
        Optional<Report> report = reportRepository.findById(id);
        if (report.isEmpty()) {
            throw new GlobalException(ReportErrorCode.INVALID_REPORT_ID);   //잘못된 reportId로 검색하는 경우에 Custom GlobalException 처리
        } else {
            return report.get();
        }
    }

    public List<Report> findAll() { //DB에 있는 모든 Report 조회
        return reportRepository.findAll();
    }
}
