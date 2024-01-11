package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.GoodPriceGoogleMapStoreService;
import com.nainga.nainga.domain.store.application.MobeomGoogleMapStoreService;
import com.nainga.nainga.domain.store.application.SafeGoogleMapStoreService;
import com.nainga.nainga.domain.store.dto.CreateDividedGoodPriceStoresResponse;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.domain.store.dto.CreateDividedSafeStoresResponse;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final MobeomGoogleMapStoreService mobeomGoogleMapStoreService;
    private final SafeGoogleMapStoreService safeGoogleMapStoreService;
    private final GoodPriceGoogleMapStoreService goodPriceGoogleMapStoreService;

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "모범음식점 데이터 생성", description = "[WARNING] DB에 처음으로 모든 모범음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/mobeom")
    public Result<String> createAllMobeomStores(@RequestParam(value = "fileName") String fileName) {
        mobeomGoogleMapStoreService.createAllMobeomStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 모범음식점 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 모범음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/dividedMobeom")
    public Result<CreateDividedMobeomStoresResponse> createDividedMobeomStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedMobeomStoresResponse response = mobeomGoogleMapStoreService.createDividedMobeomStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "안심식당 데이터 생성", description = "[WARNING] DB에 처음으로 모든 안심식당 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/safe")
    public Result<String> createAllSafeStores(@RequestParam(value = "fileName") String fileName) {
        safeGoogleMapStoreService.createAllSafeStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 안심식당 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 안심식당 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/dividedSafe")
    public Result<CreateDividedSafeStoresResponse> createDividedSafeStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedSafeStoresResponse response = safeGoogleMapStoreService.createDividedSafeStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "착한가격업소 데이터 생성", description = "[WARNING] DB에 처음으로 모든 착한가격업소 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/goodPrice")
    public Result<String> createAllGoodPriceStores(@RequestParam(value = "fileName") String fileName) {
        goodPriceGoogleMapStoreService.createAllGoodPriceStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 착한가격업소 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 착한가격업소 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!")
    @GetMapping("api/v1/store/dividedGoodPrice")
    public Result<CreateDividedGoodPriceStoresResponse> createDividedGoodPriceStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedGoodPriceStoresResponse response = goodPriceGoogleMapStoreService.createDividedGoodPriceStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }
}

