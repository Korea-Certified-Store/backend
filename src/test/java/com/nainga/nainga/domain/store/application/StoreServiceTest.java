package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StoreServiceTest {

    @Autowired
    StoreService storeService;

    @Autowired
    StoreRepository storeRepository;

    @Test
    public void autocorrect() throws Exception {    //검색어 자동 완성 기능에 대한 테스트
        //given
        Store store1 = Store.builder()
                .displayName("^")
                .build();
        Store store2 = Store.builder()
                .displayName("^^")
                .build();
        Store store3 = Store.builder()
                .displayName("^*^")
                .build();
        storeRepository.save(store1);
        storeRepository.save(store2);
        storeRepository.save(store3);

        //when
        List<String> result1 = storeService.autocorrect("^");
        List<String> result2 = storeService.autocorrect("^^");

        //then
        assertArrayEquals(result1.toArray(), List.of("^", "^^", "^*^").toArray());
        assertArrayEquals(result2.toArray(), List.of("^^").toArray());
    }
}