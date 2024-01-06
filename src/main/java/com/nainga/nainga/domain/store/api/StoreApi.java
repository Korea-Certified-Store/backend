package com.nainga.nainga.domain.store.api;

import com.nainga.nainga.domain.store.application.GoogleMapStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoreApi {
    private final GoogleMapStoreService googleMapStoreService;
}
