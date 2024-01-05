package com.nainga.nainga.domain.store.dto;

import lombok.Data;

/*
각 인증제별 데이터셋을 파싱하고 난 뒤 추출한 데이터 형식
 */
@Data
public class StoreDataByParser {
    String name;    //가게 이름
    String address;     //가게 주소

    public StoreDataByParser() {
    }

    public StoreDataByParser(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
