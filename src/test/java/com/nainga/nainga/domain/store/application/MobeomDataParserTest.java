package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MobeomDataParserTest {

    //허수가 들어있는 테스트용 Mobeom Excel 파일이 제공되었을 때, 영업중인 가게의 이름과 주소를 잘 반환하는지 테스트
    @Test
    public void getAllMobeomStoresTest() {
        //given
        //기존 getAllMobeomStores 메서드에 지정되어있는 path에서 Excel file을 읽어들임

        //when
        List<StoreDataByParser> allMobeomStores = MobeomDataParser.getAllMobeomStores("mobeom_test.xlsx");

        //then
        assertThat(allMobeomStores.size()).isGreaterThan(0); //조회된 가게가 1개 이상 있어야한다.
        allMobeomStores.stream().forEach(store -> { //조회된 가게들을 모두 조회하면서
            assertThat(store.getName()).isNotNull();    //각 가게들의 이름 중에 널이 없는지
            assertThat(store.getAddress()).isNotNull();     //각 가게들의 주소 중에 널이 없는지 검증한다.
        });
    }
}