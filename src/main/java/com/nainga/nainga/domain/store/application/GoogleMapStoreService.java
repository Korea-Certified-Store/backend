package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleMapStoreService {
    private final StoreRepository storeRepository;
}
