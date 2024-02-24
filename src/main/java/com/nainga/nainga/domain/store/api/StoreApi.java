package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.GoodPriceGoogleMapStoreService;
import com.nainga.nainga.domain.store.application.MobeomGoogleMapStoreService;
import com.nainga.nainga.domain.store.application.SafeGoogleMapStoreService;
import com.nainga.nainga.domain.store.application.StoreService;
import com.nainga.nainga.domain.store.dto.CreateDividedGoodPriceStoresResponse;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.domain.store.dto.CreateDividedSafeStoresResponse;
import com.nainga.nainga.domain.storecertification.dto.StoreCertificationsByLocationResponse;
import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final MobeomGoogleMapStoreService mobeomGoogleMapStoreService;
    private final SafeGoogleMapStoreService safeGoogleMapStoreService;
    private final GoodPriceGoogleMapStoreService goodPriceGoogleMapStoreService;
    private final StoreService storeService;

//    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "모범음식점 데이터 생성", description = "[WARNING] DB에 처음으로 모든 모범음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 모범음식점 데이터 엑셀 파일명<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/mobeom/v1")
    public Result<String> createAllMobeomStores(@RequestParam(value = "fileName") String fileName) {
        mobeomGoogleMapStoreService.createAllMobeomStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

//    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 모범음식점 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 모범음식점 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 모범음식점 데이터 엑셀 파일명<br>" +
            "dollars: 현재 API 호출에 사용할 수 있는 남은 달러<br>" +
            "startIndex: 엑셀 데이터로부터 추출한 가게 리스트 Index number를 기준으로 조회를 시작할 Index number<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/dividedMobeom/v1")
    public Result<CreateDividedMobeomStoresResponse> createDividedMobeomStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedMobeomStoresResponse response = mobeomGoogleMapStoreService.createDividedMobeomStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "안심식당 데이터 생성", description = "[WARNING] DB에 처음으로 모든 안심식당 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 안심식당 데이터 엑셀 파일명<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/safe/v1")
    public Result<String> createAllSafeStores(@RequestParam(value = "fileName") String fileName) {
        safeGoogleMapStoreService.createAllSafeStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 안심식당 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 안심식당 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 안심식당 데이터 엑셀 파일명<br>" +
            "dollars: 현재 API 호출에 사용할 수 있는 남은 달러<br>" +
            "startIndex: 엑셀 데이터로부터 추출한 가게 리스트 Index number를 기준으로 조회를 시작할 Index number<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/dividedSafe/v1")
    public Result<CreateDividedSafeStoresResponse> createDividedSafeStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedSafeStoresResponse response = safeGoogleMapStoreService.createDividedSafeStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "착한가격업소 데이터 생성", description = "[WARNING] DB에 처음으로 모든 착한가격업소 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 착한가격업소 데이터 엑셀 파일명<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/goodPrice/v1")
    public Result<String> createAllGoodPriceStores(@RequestParam(value = "fileName") String fileName) {
        goodPriceGoogleMapStoreService.createAllGoodPriceStores(fileName);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

    @Hidden
    @Tag(name = "초기 Data 생성")
    @Operation(summary = "지정한 Credit까지만 사용하여 착한가격업소 데이터 생성", description = "[WARNING] 지정한 Credit까지만 사용하여 그동안 DB에 착한가격업소 데이터를 주입해주는 API입니다. DB에 엄청난 부하가 가는 작업으로, 합의 없이 실행시켜선 안됩니다!<br><br>" +
            "[Request Param]<br>" +
            "fileName: 파싱할 착한가격업소 데이터 엑셀 파일명<br>" +
            "dollars: 현재 API 호출에 사용할 수 있는 남은 달러<br>" +
            "startIndex: 엑셀 데이터로부터 추출한 가게 리스트 Index number를 기준으로 조회를 시작할 Index number<br>" +
            "[Response Body]<br>" +
            "성공했다면, 200 http status value<br>" +
            "[Exceptions]<br>" +
            "- Excel 파일 이외의 확장자가 들어왔을 경우<br>" +
            "httpStatusValue: 404<br>" +
            "httpStatusCode: INVALID_FILE_EXTENSION<br>" +
            "message: There are incorrect files. Only the extension of Excel file is allowed.<br>" +
            "- Google Map API 연동 과정 중 오류가 발생했을 경우<br>" +
            "httpStatusValue: 500<br>" +
            "httpStatusCode: GOOGLE_MAP_SERVER_ERROR<br>" +
            "message: There are internal server errors related to Google Map API.<br>")
    @GetMapping("api/store/dividedGoodPrice/v1")
    public Result<CreateDividedGoodPriceStoresResponse> createDividedGoodPriceStores(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "dollars") double dollars, @RequestParam(value = "startIndex") int startIndex) {
        CreateDividedGoodPriceStoresResponse response = goodPriceGoogleMapStoreService.createDividedGoodPriceStores(fileName, dollars, startIndex);
        System.out.println("response = " + response);   //편하게 콘솔 로그에서 확인하기 위한 용도
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, response);
    }

    //검색어를 이용해 가게 이름에 대해 검색하여 나온 검색 결과를 바탕으로 검색어를 자동 완성해서 최대 10개의 자동 완성된 검색어를 리턴
    @Tag(name = "[New] 검색어 자동 완성")
    @Operation(summary = "사용자의 검색 키워드를 바탕으로 검색어 자동 완성", description = "사용자의 검색 키워드를 바탕으로 DB에서 매칭되는 가게 이름을 조회하여 최대 10개까지 검색어를 자동으로 완성하여 반환해줍니다.<br><br>" +
            "[Request Body]<br>" +
            "searchKeyword: 사용자의 검색 키워드<br>" +
            "[Response Body]<br>" +
            "자동으로 완성된 최대 10개의 검색어<br>")
    @GetMapping("api/store/autocorrect/v1")
    public Result<List<String>> autocorrect(@RequestParam(value = "searchKeyword") String searchKeyword) {
        List<String> autocorrectResult = storeService.autocorrect(searchKeyword);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, autocorrectResult);
    }
}

