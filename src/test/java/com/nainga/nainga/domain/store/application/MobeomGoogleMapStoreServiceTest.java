package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
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
class MobeomGoogleMapStoreServiceTest {

    @Autowired
    MobeomGoogleMapStoreService mobeomGoogleMapStoreService;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    CertificationRepository certificationRepository;

    @Autowired
    StoreCertificationRepository storeCertificationRepository;

    @Test
    void createAllMobeomStores() {
        //given
        //테스트용 mobeom_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게, 폐업된 가게, 모범 음식점에 선정되었다가 취소된 가게 등의 케이스를 모두 포함하고 있다.
        mobeomGoogleMapStoreService.createAllMobeomStores("mobeom_test.xlsx");

        //when
        List<Store> stores = storeRepository.findAll();
        List<Certification> certifications = certificationRepository.findAll();
        List<StoreCertification> storeCertifications = storeCertificationRepository.findAll();
        Optional<Store> cheongjinok = storeRepository.findByDisplayName("청진옥");
        Optional<Store> maknae = storeRepository.findByDisplayName("막내회집 광교점");
        Optional<Store> onejo = storeRepository.findByDisplayName("원조할머니 낙지센타");
        Optional<Store> hamhueng = storeRepository.findByDisplayName("함흥곰보냉면");

        //then
        assertThat(stores.size()).isEqualTo(2);
        assertThat(certifications.size()).isEqualTo(1);
        assertThat(storeCertifications.size()).isEqualTo(2);
        assertThat(cheongjinok).isEmpty();
        assertThat(maknae).isPresent();
        assertThat(onejo).isEmpty();
        assertThat(hamhueng).isPresent();
    }

    @Test
    void createDividedMobeomStores() {
        //given
        //테스트용 mobeom_test.xlsx가 주어졌을 때!
        //이 테스트 파일은 중복된 가게, 폐업된 가게, 모범 음식점에 선정되었다가 취소된 가게 등의 케이스를 모두 포함하고 있다.
        CreateDividedMobeomStoresResponse result = mobeomGoogleMapStoreService.createDividedMobeomStores("mobeom_test.xlsx", 1000, 0);

        //when

        //then
        assertThat(Math.floor(result.getDollars())).isEqualTo(999);
        assertThat(result.getNextIndex()).isEqualTo(-1);    //한 싸이클 모두 조회가 되어야 함
    }
}