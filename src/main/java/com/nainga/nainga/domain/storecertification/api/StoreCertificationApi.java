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

@RestController
@RequiredArgsConstructor
public class StoreCertificationApi {
    private final StoreCertificationService storeCertificationService;

    //북서쪽, 남동쪽 좌표를 받아 두 좌표로 만들어지는 가장 작은 사각형 내 모든 가게 상세 정보를 반환
    @Tag(name = "가게 상세 정보")
    @Operation(summary = "사용자 위치 기반 가게 상세 정보 제공", description = "사용자 위치를 기준으로 좌상단, 우하단 위도 경도 좌표를 넘겨 받아 두 좌표로 만들 수 있는 최소 크기의 사각형 범위 내 모든 가게의 상세 정보를 전달해줍니다.<br><br>" +
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
    @GetMapping("api/v1/storecertification/byLocation")
    public Result<List<StoreCertificationsByLocationResponse>> findStoreCertificationsByLocation(@RequestParam("nwLong") double nwLong, @RequestParam("nwLat") double nwLat, @RequestParam("seLong") double seLong, @RequestParam("seLat") double seLat) {
        List<StoreCertification> storeCertificationsByLocation = storeCertificationService.findStoreCertificationsByLocation(new Location(nwLong, nwLat), new Location(seLong, seLat));
        List<Long> storeIdsWithMultipleCertifications = storeCertificationService.findStoreIdsWithMultipleCertifications(); //여러 인증제를 가지고 있는 가게의 id 리스트
        List<StoreCertificationsByLocationResponse> storeCertificationsByLocationResponses = new ArrayList<>(); //반환해줄 StoreCertificationsByLocationResponse들의 List
        Map<Long, StoreCertificationsByLocationResponse> map = new HashMap<>(); //여러 인증제를 가지고 있는 가게들의 response를 임시로 저장하고 있을 map

        for (StoreCertification storeCertification : storeCertificationsByLocation) {
            if (!storeIdsWithMultipleCertifications.contains(storeCertification.getStore().getId())) {
                storeCertificationsByLocationResponses.add(new StoreCertificationsByLocationResponse(storeCertification));
            } else {
                StoreCertificationsByLocationResponse storeCertificationResult = map.get(storeCertification.getStore().getId());
                if (storeCertificationResult != null) {
                    storeCertificationResult.getCertificationName().add(storeCertification.getCertification().getName());
                } else {
                    map.put(storeCertification.getStore().getId(), new StoreCertificationsByLocationResponse(storeCertification));
                }
            }
        }

        map.forEach((key,value) -> {    //여러 인증제를 가지고 있는 가게들의 Responses들도 최종 반환해줄 List에 추가
            storeCertificationsByLocationResponses.add(value);
        });

        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, storeCertificationsByLocationResponses);
    }
}
