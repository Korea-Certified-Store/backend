package com.nainga.nainga.domain.report.application;

import com.nainga.nainga.domain.report.dao.ReportRepository;
import com.nainga.nainga.domain.report.domain.NewStoreReport;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
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
}
