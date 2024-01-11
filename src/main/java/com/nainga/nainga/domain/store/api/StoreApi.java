package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.MobeomGoogleMapStoreService;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final MobeomGoogleMapStoreService mobeomGoogleMapStoreService;

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "모범 음식점 데이터 생성", description = "[WARNING] DB에 처음으로 모든 모범 음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/mobeom")
    public Result<String> createAllMobeomStores(@RequestParam(value = "fileName") String fileName) {
        mobeomGoogleMapStoreService.createAllMobeomStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 모범 음식점 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 모범 음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/dividedMobeom")
    public Result<CreateDividedMobeomStoresResponse> createDividedMobeomStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedMobeomStoresResponse response = mobeomGoogleMapStoreService.createDividedMobeomStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }
}
