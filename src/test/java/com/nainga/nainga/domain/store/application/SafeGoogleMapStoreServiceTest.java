package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedSafeStoresResponse;
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
class SafeGoogleMapStoreServiceTest {

    @Autowired
    SafeGoogleMapStoreService safeGoogleMapStoreService;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    CertificationRepository certificationRepository;

    @Autowired
    StoreCertificationRepository storeCertificationRepository;

    @Test
    void createAllSafeStores() {
        //given
        //테스트용 safe_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게, 지정 취소된 가게 등의 케이스를 모두 포함하고 있다.
        safeGoogleMapStoreService.createAllSafeStores("safe_test.xlsx");

        //when
        List<Store> stores = storeRepository.findAll();
        List<Certification> certifications = certificationRepository.findAll();
        List<StoreCertification> storeCertifications = storeCertificationRepository.findAll();
        Optional<Store> chilbolock = storeRepository.findByDisplayName("칠보락");
        Optional<Store> leebadom = storeRepository.findByDisplayName("이바돔 거제법원점");
        Optional<Store> dongoon = storeRepository.findByDisplayName("돈군");
        Optional<Store> oriya = storeRepository.findByDisplayName("오리야");
        Optional<Store> pohang = storeRepository.findByDisplayName("포항물회");

        //then
        assertThat(stores.size()).isEqualTo(4);
        assertThat(certifications.size()).isEqualTo(1);
        assertThat(storeCertifications.size()).isEqualTo(4);
        assertThat(chilbolock).isPresent();
        assertThat(leebadom).isPresent();
        assertThat(dongoon).isEmpty();
        assertThat(oriya).isPresent();
        assertThat(pohang).isPresent();
    }

    @Test
    void createDividedSafeStores() {
        //given
        //테스트용 safe_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게, 지정 취소된 가게 등의 케이스를 모두 포함하고 있다.
        CreateDividedSafeStoresResponse result = safeGoogleMapStoreService.createDividedSafeStores("safe_test.xlsx", 1000, 0);

        //when

        //then
        assertThat(Math.floor(result.getDollars())).isEqualTo(999);
        assertThat(result.getNextIndex()).isEqualTo(-1);    //한 싸이클 모두 조회가 되어야 함

        //그 뒤로 바로 이어서 동일한 메서드를 추가 호출했을 때, 이미 DB에 등록된 상태이므로 API call이 나가지 않아야 함
        CreateDividedSafeStoresResponse result2 = safeGoogleMapStoreService.createDividedSafeStores("safe_test.xlsx", 1000, 0);
        assertThat(result2.getDollars()).isEqualTo(1000);   //API call이 나가지 않아서 비용 그대로!
        assertThat(result2.getNextIndex()).isEqualTo(-1);   //한 싸이클은 전부 조회하므로 -1 리턴
    }
}