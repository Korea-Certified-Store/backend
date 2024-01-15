package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedGoodPriceStoresResponse;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  //테스트가 끝난 뒤 롤백하기 위해
class GoodPriceGoogleMapStoreServiceTest {

    @Autowired
    GoodPriceGoogleMapStoreService goodPriceGoogleMapStoreService;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    CertificationRepository certificationRepository;

    @Autowired
    StoreCertificationRepository storeCertificationRepository;

    @Test
    void createAllGoodPriceStores() {
        //given
        //테스트용 goodPrice_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게 등의 케이스를 모두 포함하고 있다.
        goodPriceGoogleMapStoreService.createAllGoodPriceStores("goodPrice_test.xlsx");

        //when
        List<Store> stores = storeRepository.findAll();
        List<Certification> certifications = certificationRepository.findAll();
        List<StoreCertification> storeCertifications = storeCertificationRepository.findAll();
        Optional<Store> cheonggukjang = storeRepository.findByDisplayName("청국장이랑갈비찜");
        Optional<Store> jeju = storeRepository.findByDisplayName("제주복집");
        Optional<Store> jeonju = storeRepository.findByDisplayName("전주식당");
        Optional<Store> boyak = storeRepository.findByDisplayName("보약족발");
        Optional<Store> lanya = storeRepository.findByDisplayName("라냐");

        //then
        assertThat(stores.size()).isEqualTo(4);
        assertThat(certifications.size()).isEqualTo(1);
        assertThat(storeCertifications.size()).isEqualTo(4);
        assertThat(cheonggukjang).isPresent();
        assertThat(jeju).isPresent();
        assertThat(jeonju).isEmpty();
        assertThat(boyak).isPresent();
        assertThat(lanya).isPresent();
    }

    @Test
    void createDividedGoodPriceStores() {
        //given
        //테스트용 goodPrice_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게 등의 케이스를 모두 포함하고 있다.
        CreateDividedGoodPriceStoresResponse result = goodPriceGoogleMapStoreService.createDividedGoodPriceStores("goodPrice_test.xlsx", 1000, 0);

        //when

        //then
        assertThat(Math.floor(result.getDollars())).isEqualTo(999);
        assertThat(result.getNextIndex()).isEqualTo(-1);    //한 싸이클 모두 조회가 되어야 함

        //그 뒤로 바로 이어서 동일한 메서드를 추가 호출했을 때, 이미 DB에 등록된 상태이므로 API call이 나가지 않아야 함
        CreateDividedGoodPriceStoresResponse result2 = goodPriceGoogleMapStoreService.createDividedGoodPriceStores("goodPrice_test.xlsx", 1000, 0);
        assertThat(result2.getDollars()).isEqualTo(1000);   //API call이 나가지 않아서 비용 그대로!
        assertThat(result2.getNextIndex()).isEqualTo(-1);   //한 싸이클은 전부 조회하므로 -1 리턴
    }
}
