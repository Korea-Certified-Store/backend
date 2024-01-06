package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.GoogleMapStoreService;
import com.nainga.nainga.global.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final GoogleMapStoreService googleMapStoreService;

    @GetMapping("api/v1/store/mobeom")
    public Result<String> createAllMobeomStores() {
        googleMapStoreService.createAllMobeomStores();
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, null);
    }

}
