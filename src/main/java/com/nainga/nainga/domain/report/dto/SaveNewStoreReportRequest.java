package com.nainga.nainga.domain.report.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveNewStoreReportRequest {
    private String storeName;   //가게 이름
    private String formattedAddress;    //가게 주소
    private List<String> certifications;    //가게가 가지고 있는 인증제 이름 리스트
}
