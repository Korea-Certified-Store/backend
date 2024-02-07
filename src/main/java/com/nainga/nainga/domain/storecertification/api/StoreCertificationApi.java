package com.nainga.nainga.domain.storecertification.api;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.application.StoreCertificationService;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.domain.storecertification.dto.StoreCertificationsByLocationResponse;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
public class StoreCertificationApi {
    private final StoreCertificationService storeCertificationService;

    //총 사각형 영역의 네 꼭짓점 좌표를 받아 최대 75개를 랜덤하게 뽑아낸 뒤, 해당 가게들의 상세 정보를 반환
    @Tag(name = "가게 상세 정보")
    @Operation(summary = "사용자 위치 기반 가게 상세 정보 제공", description = "총 사각형 영역의 네 꼭짓점 좌표를 받아 최대 75개를 랜덤하게 뽑아낸 뒤, 해당 가게들의 상세 정보를 반환해줍니다.<br><br>" +
            "[Request Body]<br>" +
            "nwLong: 북서쪽 좌표 경도<br>" +
            "nwLat: 북서쪽 좌표 위도<br>" +
            "seLong: 남동쪽 좌표 경도<br>" +
            "seLat: 남동쪽 좌표 위도<br><br>" +
            "[Response Body]<br>" +
            "id: Database 내 Primary Key값<br>" +
            "displayName: 가게 이름<br>" +
            "primaryTypeDisplayName: 업종<br>" +
            "formattedAddress: 주소<br>" +
            "phoneNumber: 전화번호<br>" +
            "location: (경도, 위도) 가게 좌표<br>" +
            "regularOpeningHours: 영업 시간<br>" +
            "=> 특정 요일이 휴무인 경우에는 해당 요일에 대한 데이터가 들어있지 않습니다. Break time이 있는 경우 동일한 요일에 대해 영업 시간 데이터가 여러 개 존재할 수 있습니다. <br>" +
            "localPhotos: 저장된 가게 사진 URL<br>" +
            "certificationName: 가게의 인증제 목록<br>" +
            "=> 각 인증제별 순서는 보장되지 않습니다.")
    @GetMapping("api/v2/storecertification/byLocation")
    public Result<List<List<StoreCertificationsByLocationResponse>>> findStoreCertificationsByLocationRandomly(@RequestParam("nwLong") double nwLong, @RequestParam("nwLat") double nwLat, @RequestParam("swLong") double swLong, @RequestParam("swLat") double swLat, @RequestParam("seLong") double seLong, @RequestParam("seLat") double seLat, @RequestParam("neLong") double neLong, @RequestParam("neLat") double neLat) {
        List<List<StoreCertificationsByLocationResponse>> storeCertificationsByLocationRandomly = storeCertificationService.findStoreCertificationsByLocationRandomly(new Location(nwLong, nwLat), new Location(swLong, swLat), new Location(seLong, seLat), new Location(neLong, neLat));
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, storeCertificationsByLocationRandomly);
    }
}