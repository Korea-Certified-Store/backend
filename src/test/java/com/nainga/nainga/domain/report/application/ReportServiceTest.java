package com.nainga.nainga.domain.report.application;

import com.nainga.nainga.domain.report.dao.ReportRepository;
import com.nainga.nainga.domain.report.domain.DelSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.FixSpecificStoreReport;
import com.nainga.nainga.domain.report.domain.NewStoreReport;
import com.nainga.nainga.domain.report.domain.Report;
import com.nainga.nainga.domain.report.dto.SaveNewStoreReportRequest;
import com.nainga.nainga.domain.report.dto.SaveSpecificStoreReportRequest;
import com.nainga.nainga.global.exception.GlobalException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportService reportService;

    @Test
    public void saveNewStoreReport() throws Exception {
        //given
        SaveNewStoreReportRequest saveNewStoreReportRequest1 = new SaveNewStoreReportRequest("가게1", "주소1", List.of("착한가격업소", "모범음식점")); //정상적인 테스트 케이스
        SaveNewStoreReportRequest saveNewStoreReportRequest2 = new SaveNewStoreReportRequest("가게2", "주소2", List.of("착한가격업"));   //인증제 이름이 잘못되었을 때

        //when
        Long report1Id = reportService.saveNewStoreReport(saveNewStoreReportRequest1);
        NewStoreReport report = (NewStoreReport) reportService.findById(report1Id);

        //then
        assertArrayEquals(report.getCertifications().toArray(), saveNewStoreReportRequest1.getCertifications().toArray());
        assertThat(report.getStoreName()).isEqualTo(saveNewStoreReportRequest1.getStoreName());
        assertThat(report.getFormattedAddress()).isEqualTo(saveNewStoreReportRequest1.getFormattedAddress());
        assertThatThrownBy(() -> reportService.saveNewStoreReport(saveNewStoreReportRequest2))  //잘못된 인증제라서 예외가 터져야함
                .isInstanceOf(GlobalException.class);
    }

    @Test
    public void saveSpecificStoreReport() throws Exception {
        //given
        SaveSpecificStoreReportRequest saveSpecificStoreReportRequest1 = new SaveSpecificStoreReportRequest("del", 123L, "내용1");    //정상적인 테스트 케이스
        SaveSpecificStoreReportRequest saveSpecificStoreReportRequest2 = new SaveSpecificStoreReportRequest("fix", 1234L, "내용2");   //정상적인 테스트 케이스
        SaveSpecificStoreReportRequest saveSpecificStoreReportRequest3 = new SaveSpecificStoreReportRequest("xxx", 12345L, "내용3");  //잘못된 dtype

        //when
        Long report1Id = reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest1);
        Long report2Id = reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest2);
        DelSpecificStoreReport report1 = (DelSpecificStoreReport) reportService.findById(report1Id);
        FixSpecificStoreReport report2 = (FixSpecificStoreReport) reportService.findById(report2Id);

        //then
        assertThat(report1.getStoreId()).isEqualTo(saveSpecificStoreReportRequest1.getStoreId());
        assertThat(report2.getContents()).isEqualTo(saveSpecificStoreReportRequest2.getContents());
        assertThatThrownBy(() -> reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest3))    //잘못된 dtype이라서 예외가 터져야함
                .isInstanceOf(GlobalException.class);
    }

    @Test
    public void findById() throws Exception {
        //given
        SaveNewStoreReportRequest saveNewStoreReportRequest1 = new SaveNewStoreReportRequest("가게1", "주소1", List.of("착한가격업소", "모범음식점")); //정상적인 테스트 케이스
        SaveSpecificStoreReportRequest saveSpecificStoreReportRequest1 = new SaveSpecificStoreReportRequest("del", 123L, "내용1");    //정상적인 테스트 케이스

        //when
        Long report1Id = reportService.saveNewStoreReport(saveNewStoreReportRequest1);
        Long report2Id = reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest1);
        NewStoreReport report1 = (NewStoreReport) reportService.findById(report1Id);
        DelSpecificStoreReport report2 = (DelSpecificStoreReport) reportService.findById(report2Id);

        //then
        assertThat(report1.getFormattedAddress()).isEqualTo(saveNewStoreReportRequest1.getFormattedAddress());
        assertArrayEquals(report1.getCertifications().toArray(), saveNewStoreReportRequest1.getCertifications().toArray());
        assertThat(report1.getStoreName()).isEqualTo(saveNewStoreReportRequest1.getStoreName());
        assertThat(report2.getContents()).isEqualTo(saveSpecificStoreReportRequest1.getContents());
        assertThat(report2.getStoreId()).isEqualTo(saveSpecificStoreReportRequest1.getStoreId());
    }

    @Test
    public void findAll() throws Exception {
        //given
        SaveNewStoreReportRequest saveNewStoreReportRequest1 = new SaveNewStoreReportRequest("가게1", "주소1", List.of("착한가격업소", "모범음식점")); //정상적인 테스트 케이스
        SaveSpecificStoreReportRequest saveSpecificStoreReportRequest1 = new SaveSpecificStoreReportRequest("del", 123L, "내용1");    //정상적인 테스트 케이스

        //when
        Long report1Id = reportService.saveNewStoreReport(saveNewStoreReportRequest1);
        Long report2Id = reportService.saveSpecificStoreReport(saveSpecificStoreReportRequest1);

        List<Report> reports = reportService.findAll();
        List<Long> reportIds = reports.stream().map(Report::getId).toList();

        //then
        assertArrayEquals(reportIds.toArray(), List.of(report1Id, report2Id).toArray());

    }

}