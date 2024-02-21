package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dao.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public List<String> autocorrect(String keyword) {   //검색어 자동 완성 로직
        return storeRepository.findAllBySearchKeyword(keyword);
    }

//    private final RedisSortedSetService redisSortedSetService;
//    private final RedisHashService redisHashService;

     /*
        Redis Hash 자료 구조를 활용한 새로운 검색어 자동 완성 로직입니다.
        아래 로직은 Case insensitive하게 검색될 수 있도록 구현하였습니다.
        지금은 MySQL이 성능이 더 좋아서 사용하지 않습니다.
     */

//    @PostConstruct
//    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
//        redisHashService.removeAllOfHash();
//        saveAllDisplayName(storeRepository.findAllDisplayName());   //모든 가게명을 소문자로 변환한 것을 field, 원래 가게 이름을 value로 매핑시켜서 Redis Hash에 저장
//    }

//    public void saveAllDisplayName(List<String> allDisplayName) {
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //병렬 처리를 위한 스레드풀을 생성하는 과정
//
//        for (String displayName : allDisplayName) {
//            executorService.submit(() -> {  //submit 메서드를 사용해서 병렬 처리할 작업 추가
//                //Redis 내장 기능이 아닌 case insensitive 조회를 구현하기 위해 소문자로 변환한 field값과 원래 string인 value의 쌍을 Hash에 저장
//                redisHashService.addToHash(displayName.toLowerCase(), displayName);  //case insensitive하게 검색어 자동 완성 기능을 직접 구현하기 위해 소문자로 통일해서 저장
//            });
//        }
//        executorService.shutdown(); //작업이 모두 완료되면 스레드풀을 종료
//    }

//    public List<String> autocorrect(String keyword) {   //검색어 자동 완성 로직
//        Set<String> allValuesContainingSearchKeyword = redisHashService.findAllValuesContainingSearchKeyword(keyword);  //case insensitive하게 serachKeyword를 포함하는 가게 이름 최대 10개 반환
//        if(allValuesContainingSearchKeyword.isEmpty())
//            return new ArrayList<>();
//        else
//            return new ArrayList<>(allValuesContainingSearchKeyword);   //자동 완성 결과가 존재하면 ArrayList로 변환하여 리턴
//    }

    /*
        아래 주석처리 된 코드는 초기 검색어 자동 완성 로직입니다.
        검색어 자동 완성에 대한 요구 사항이 앞에서부터 매칭되는 글자가 아닌 Contains 개념으로 바뀌어서 주석 처리하여 임시로 남겨놓았습니다.
        지금은 MySQL이 성능이 더 좋기 때문에 사용하지 않는 코드입니다.
     */

//    private String suffix = "*";    //검색어 자동 완성 기능에서 실제 노출될 수 있는 완벽한 형태의 단어를 구분하기 위한 접미사
//    private int maxSize = 10;    //검색어 자동 완성 기능 최대 개수
//
//    @PostConstruct
//    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
//        redisSortedSetService.removeAllOfSortedSet();
//        saveAllSubstring(storeRepository.findAllDisplayName()); //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
//    }
//
//    public void saveAllSubstring(List<String> allDisplayName) {
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //병렬 처리를 위한 스레드풀을 생성하는 과정
//
//        for (String displayName : allDisplayName) {
//            executorService.submit(() -> {  //submit 메서드를 사용해서 병렬 처리할 작업 추가
//                redisSortedSetService.addToSortedSet(displayName + suffix);
//
//                for (int i = displayName.length(); i > 0; --i) {
//                    redisSortedSetService.addToSortedSet(displayName.substring(0, i));
//                }
//            });
//        }
//        executorService.shutdown(); //작업이 모두 완료되면 스레드풀을 종료
//    }
//
//    public List<String> autocorrect(String keyword) { //검색어 자동 완성 기능 관련 로직
//        Long index = redisSortedSetService.findFromSortedSet(keyword);  //사용자가 입력한 검색어를 바탕으로 Redis에서 조회한 결과 매칭되는 index
//
//        if (index == null) {
//            return new ArrayList<>();   //만약 사용자 검색어 바탕으로 자동 완성 검색어를 만들 수 없으면 Empty Array 리턴
//        }
//
//        Set<String> allValuesAfterIndexFromSortedSet = redisSortedSetService.findAllValuesAfterIndexFromSortedSet(index);   //사용자 검색어 이후로 정렬된 Redis 데이터들 가져오기
//
//        return allValuesAfterIndexFromSortedSet.stream()
//                .filter(value -> value.endsWith(suffix) && value.startsWith(keyword))
//                .map(value -> StringUtils.removeEnd(value, suffix))
//                .limit(maxSize)
//                .toList();  //자동 완성을 통해 만들어진 최대 maxSize개의 키워드들
//    }
}
