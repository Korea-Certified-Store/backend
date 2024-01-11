package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.application.GoogleMapStoreService;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisabledIfSystemProperty(named = "mysql", matches = "false")    //Github Actions Workflow 파일에서 해당 System property를 false로 넘겨주어 skip!
@TestPropertySource(locations = "classpath:application-mysql.yml", properties = "spring.profiles.active=mysql") //해당 테스트 클레스에 대해서는 application-mysql.yml이 적용될 수 있도록 active와 path 설정
//테스트용 DB로 사용하는 H2 DB에서는 아래 MBR쿼리를 실행시킬 수 없으므로, 아래 테스트는 mysql 전용 properties를 사용하도록 설정
//하지만 이렇게하면, Github Actions 상에서 돌아가는 테스트의 경우 Local MySQL이 없으므로 테스트가 실패할 것이다.
//따라서 아래 테스트를 Skip 할 수 있도록 mysql이라는 System property를 false로 넘겨주어 Skip하도록 하였다. 이건 내가 임의로 정한 proerty key와 value이다.
class StoreCertificationServiceTest {
    @Autowired
    GoogleMapStoreService googleMapStoreService;
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
        googleMapStoreService.createAllMobeomStores("mobeom_test.xlsx");
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