package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.global.application.RedisSortedSetService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final RedisSortedSetService redisSortedSetService;

    @PostConstruct
    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
        int pageSize = 1000;
        int page = 0;

        while (true) {
            storeRepository
        }

    }
}
