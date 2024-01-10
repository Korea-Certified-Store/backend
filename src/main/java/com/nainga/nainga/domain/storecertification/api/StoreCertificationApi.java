package com.nainga.nainga.domain.storecertification.api;

import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.storecertification.application.StoreCertificationService;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.domain.storecertification.dto.StoreCertificationsByLocationRequest;
import com.nainga.nainga.domain.storecertification.dto.StoreCertificationsByLocationResponse;
import com.nainga.nainga.global.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StoreCertificationApi {
    private final StoreCertificationService storeCertificationService;

    //북서쪽, 남동쪽 좌표를 받아 두 좌표로 만들어지는 가장 작은 사각형 내 모든 가게 상세 정보를 반환
    @PostMapping("api/v1/storecertification/byLocation")
    public Result<List<StoreCertificationsByLocationResponse>> findStoreCertificationsByLocation(@RequestBody StoreCertificationsByLocationRequest storeCertificationsByLocationRequest) {
        List<StoreCertification> storeCertificationsByLocation = storeCertificationService.findStoreCertificationsByLocation(storeCertificationsByLocationRequest.getNorthWestLocation(), storeCertificationsByLocationRequest.getSouthEastLocation());
        List<StoreCertificationsByLocationResponse> storeCertificationsByLocationResponse = storeCertificationsByLocation.stream()
                .map(StoreCertificationsByLocationResponse::new)
                .collect(Collectors.toList());
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, storeCertificationsByLocationResponse);
    }
}
