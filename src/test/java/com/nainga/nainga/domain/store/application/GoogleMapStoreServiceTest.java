package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  //테스트가 끝난 뒤 롤백하기 위해
class GoogleMapStoreServiceTest {

    @Autowired
    GoogleMapStoreService googleMapStoreService;

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
        googleMapStoreService.createAllMobeomStores("mobeom_test.xlsx");

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
        CreateDividedMobeomStoresResponse result = googleMapStoreService.createDividedMobeomStores("mobeom_test.xlsx", 1000, 0);

        //when

        //then
        assertThat(Math.floor(result.getDollars() * 10000) / 10000).isEqualTo(999.9460);    //부동 소수점 계산 오차 때문에 소수점 넷째자리까지만 표현
        assertThat(result.getNextIndex()).isEqualTo(-1);    //한 싸이클 모두 조회가 되어야 함

        //그 뒤로 바로 이어서 동일한 메서드를 추가 호출했을 때, 이미 DB에 등록된 상태이므로 API call이 나가지 않아야 함
        CreateDividedMobeomStoresResponse result2 = googleMapStoreService.createDividedMobeomStores("mobeom_test.xlsx", 1000, 0);
        assertThat(result2.getDollars()).isEqualTo(1000);   //API call이 나가지 않아서 비용 그대로!
        assertThat(result2.getNextIndex()).isEqualTo(-1);   //한 싸이클은 전부 조회하므로 -1 리턴
    }
}