package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.global.application.RedisSortedSetService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final RedisSortedSetService redisSortedSetService;
    private String suffix = "*";    //검색어 자동 완성 기능에서 실제 노출될 수 있는 완벽한 형태의 단어를 구분하기 위한 접미사
    private int maxSize = 5;    //검색어 자동 완성 기능 최대 개수

    @PostConstruct
    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
        saveAllSubstring(storeRepository.findAllDisplayName()); //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
    }

    private void saveAllSubstring(List<Store> allDisplayName) { //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
        for (Store displayName : allDisplayName) {
            redisSortedSetService.addToSortedSet(displayName.getDisplayName() + suffix);   //완벽한 형태의 단어일 경우에는 *을 붙여 구분

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

        Set<String> allValuesAfterIndexFromSortedSet = redisSortedSetService.findAllValuesAfterIndexFromSortedSet(index);   //사용자 검색어 이후로 정렬된 Redis 데이터들 가져오기

        List<String> autocorrectKeywords = allValuesAfterIndexFromSortedSet.stream()
                .filter(value -> value.endsWith(suffix) && value.startsWith(keyword))
                .map(value -> StringUtils.removeEnd(value, suffix))
                .limit(maxSize)
                .toList();  //자동 완성을 통해 만들어진 최대 maxSize개의 키워드들

        return autocorrectKeywords;
    }
}
