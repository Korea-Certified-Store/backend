package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.global.application.RedisSortedSetService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StoreServiceTest {

    @Autowired
    StoreService storeService;

    @Autowired
    RedisSortedSetService redisSortedSetService;

    @Test
    public void autocorrect() throws Exception {    //검색어 자동 완성 기능에 대한 테스트
        //given
        //테스트를 실행시키는 환경에 따라 잘못된 결과가 나올 수 있으므로 테스트용 가게 이름 제일 앞에는 실제 Production DB에 존재하지 않는 이름인 *을 붙여 사용
        List<String> allDisplayName = List.of("*김밥천국", "*김밥나라", "*김빱월드", "*김밥천지"); //List의 팩토리 메서드 사용
        System.out.println("allDisplayName = " + allDisplayName);
        storeService.saveAllSubstring(allDisplayName);  //검색어 자동 완성 기능을 위해 필요한 Substring들을 뽑아 Redis에 저장

        //when
        List<String> resultByKim = storeService.autocorrect("*김");   //Redis 상에 사전순 정렬되어 있으므로 *김밥나라, *김밥천국, *김밥천지, *김빱월드 순으로 나옴
        List<String> resultByKimBap = storeService.autocorrect("*김밥");   //*김밥나라, *김밥천국, *김밥천지가 나와야 함
        List<String> resultByKimBapCheon = storeService.autocorrect("*김밥천"); //*김밥천국, *김밥천지가 나와야 함
        List<String> resultByKimBapCheonGuk = storeService.autocorrect("*김밥천국"); //*김밥천국이 나와야 함

        //then
        assertThat(resultByKim).containsExactly("*김밥나라", "*김밥천국", "*김밥천지", "*김빱월드");
        assertThat(resultByKimBap).containsExactly("*김밥나라", "*김밥천국", "*김밥천지");
        assertThat(resultByKimBapCheon).containsExactly("*김밥천국", "*김밥천지");
        assertThat(resultByKimBapCheonGuk).containsExactly("*김밥천국");
    }
}