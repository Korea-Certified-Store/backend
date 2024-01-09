package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.GoogleMapStoreService;
import com.nainga.nainga.domain.store.application.StoreService;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.domain.store.dto.StoresByLocationRequest;
import com.nainga.nainga.domain.store.dto.StoresByLocationResponse;
import com.nainga.nainga.global.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final GoogleMapStoreService googleMapStoreService;
    private final StoreService storeService;

    @GetMapping("api/v1/store/mobeom")
    public Result<String> createAllMobeomStores(@RequestParam(value = "fileName") String fileName) {
        googleMapStoreService.createAllMobeomStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @GetMapping("api/v1/store/dividedMobeom")
    public Result<CreateDividedMobeomStoresResponse> createDividedMobeomStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedMobeomStoresResponse response = googleMapStoreService.createDividedMobeomStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    //북서쪽, 남동쪽 좌표를 받아 두 좌표로 만들어지는 가장 작은 사각형 내 모든 가게 상세 정보를 반환
    @PostMapping("api/v1/store/byLocation")
    public Result<List<StoresByLocationResponse>> findStoresByLocation(@RequestBody StoresByLocationRequest storesByLocationRequest) {
        List<Store> storesByLocation = storeService.findStoresByLocation(storesByLocationRequest.getNorthWestLocation(), storesByLocationRequest.getSouthEastLocation());
        List<StoresByLocationResponse> storesByLocationResponse = storesByLocation.stream()
                .map(StoresByLocationResponse::new)
                .collect(Collectors.toList());
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, storesByLocationResponse);
    }


}
