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

    //총 사각형 영역의 네 꼭짓점 좌표를 받아, 해당 가게들의 상세 정보를 반환
    @Tag(name = "가게 상세 정보")
    @Operation(summary = "사용자 위치 기반 가게 상세 정보 제공 V1", description = "총 사각형 영역의 네 꼭짓점 좌표를 받아 해당 가게들의 상세 정보를 반환해줍니다.<br><br>" +
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
    public Result<List<StoreCertificationsByLocationResponse>> findStoreCertificationsByLocation(@RequestParam("nwLong") double nwLong, @RequestParam("nwLat") double nwLat, @RequestParam("swLong") double swLong, @RequestParam("swLat") double swLat, @RequestParam("seLong") double seLong, @RequestParam("seLat") double seLat, @RequestParam("neLong") double neLong, @RequestParam("neLat") double neLat) {
        List<StoreCertification> storeCertificationsByLocation = storeCertificationService.findStoreCertificationsByLocation(new Location(nwLong, nwLat), new Location(swLong, swLat), new Location(seLong, seLat), new Location(neLong, neLat));
        List<Long> storeIdsWithMultipleCertifications = storeCertificationService.getDuplicatedStoreIds(); //여러 인증제를 가지고 있는 가게의 id 리스트
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

    //총 사각형 영역의 네 꼭짓점 좌표를 받아 최대 75개를 랜덤하게 뽑아낸 뒤, 해당 가게들의 상세 정보를 반환
    @Tag(name = "가게 상세 정보")
    @Operation(summary = "사용자 위치 기반 가게 상세 정보 제공 V2", description = "총 사각형 영역의 네 꼭짓점 좌표를 받아 최대 75개를 랜덤하게 뽑아낸 뒤, 해당 가게들의 상세 정보를 반환해줍니다.<br><br>" +
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

    //검색어를 이용해 가게 이름, 업종, 주소에 대해 검색하고 나온 검색 결과 중 사용자로부터 가까운 순으로 최대 30개의 가게 정보를 리턴
    @Tag(name = "가게 상세 정보")
    @Operation(summary = "사용자 위치 및 검색어 기반 가게 상세 정보 제공", description = "현재 사용자의 위치 좌표와 검색 키워드를 전달받아 가게 DB에서 검색한 뒤, 사용자와 가까운 순으로 최대 30개의 가게 상세 정보를 반환해줍니다.<br><br>" +
            "[Request Body]<br>" +
            "currLong: 현재 사용자의 위치 좌표 경도값<br>" +
            "currLat: 현재 사용자의 위치 좌표 위도값<br>" +
            "searchKeyword: 검색할 키워드<br><br>" +
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
    @GetMapping("api/v1/storecertification/byLocationAndKeyword")
    public Result<List<StoreCertificationsByLocationResponse>> searchStoreCertificationsByLocationAndKeyword(@RequestParam("currLong") double currLong, @RequestParam("currLat") double currLat, @RequestParam("searchKeyword") String searchKeyword) {
        List<StoreCertificationsByLocationResponse> storeCertificationsByLocationAndKeyword = storeCertificationService.searchStoreCertificationsByLocationAndKeyword(currLong, currLat, searchKeyword);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, storeCertificationsByLocationAndKeyword);
    }
}