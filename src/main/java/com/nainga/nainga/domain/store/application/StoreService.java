package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
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
        saveAllSubstring(storeRepository.findAllDisplayName()); //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
    }

    private void saveAllSubstring(List<Store> allDisplayName) { //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
        for (Store displayName : allDisplayName) {
            redisSortedSetService.addToSortedSet(displayName.getDisplayName() + "*");   //완벽한 형태의 단어일 경우에는 *을 붙여 구분

            for (int i = displayName.getDisplayName().length()-1; i > 0; --i) { //음절 단위로 잘라서 모든 Substring 구하기
                redisSortedSetService.addToSortedSet(displayName.getDisplayName().substring(0, i)); //곧바로 redis에 저장
            }
        }
    }

    public List<String> autocorrect(String keyword) { //검색어 자동 완성 기능 관련 로직
        Long index = redisSortedSetService.findFromSortedSet(keyword);  //사용자가 입력한 검색어를 바탕으로 Redis에서 조회한 결과 매칭되는 index

        if (index == null) {
            return new ArrayList<>();   //만약 사용자 검색어 바탕으로 자동 완성 검색어를 만들 수 없으면 Empty Array 리턴
        }


    }
}
