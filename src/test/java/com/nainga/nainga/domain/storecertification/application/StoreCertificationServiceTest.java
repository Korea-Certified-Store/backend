package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.application.MobeomGoogleMapStoreService;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StoreCertificationServiceTest {
    @Autowired
    MobeomGoogleMapStoreService mobeomGoogleMapStoreService;
    @Autowired
    StoreCertificationService storeCertificationService;
    @Autowired
    StoreCertificationRepository storeCertificationRepository;
    @Autowired
    StoreRepository storeRepository;

    @Test
    void findStoreCertificationsByLocation() {
        //findStoreCertificationsByLocatin()을 테스트하기 위해 DB에 있는 모든 가게를 조회해서 최소 최대 경도 위도값을 구하고 그 값들보다 바깥쪽 범위의 경도 위도를 통해 모든 가게가 찾아지는지 검증

        //given
        //테스트용 mobeom_test.xlsx가 주어졌을 때!
        mobeomGoogleMapStoreService.createAllMobeomStores("mobeom_test.xlsx");
        List<Store> stores = storeRepository.findAll();
        double minLongitude=999; double minLatitude=999; double maxLongitude=0; double maxLatitude=0;

        for (Store store : stores) {    //존재하는 가게들 중에서 최소 최대 경도 위도값 구하기
            Point location = store.getLocation();
            if (location.getX() < minLongitude) {
                minLongitude = location.getX();
            }
            if (location.getX() > maxLongitude) {
                maxLongitude = location.getX();
            }
            if (location.getY() < minLatitude) {
                minLatitude = location.getY();
            }
            if (location.getY() > maxLatitude) {
                maxLatitude = location.getY();
            }
        }

        //when
        Location location1 = new Location(minLongitude - 1.0, minLatitude - 1.0);
        Location location2 = new Location(maxLongitude + 1.0, maxLatitude + 1.0);
        List<StoreCertification> storeCertificationsByLocation = storeCertificationService.findStoreCertificationsByLocation(location1, location2);

        //then
        assertThat(storeCertificationsByLocation.size()).isEqualTo(stores.size());
    }
}