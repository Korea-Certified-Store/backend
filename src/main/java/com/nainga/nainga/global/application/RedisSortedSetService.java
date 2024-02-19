//package com.nainga.nainga.global.application;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class RedisSortedSetService {    //검색어 자동 완성을 구현할 때 사용하는 Redis의 SortedSet 관련 서비스 레이어
//    private final RedisTemplate<String, String> redisTemplate;
//    private String key = "autocorrect"; //검색어 자동 완성을 위한 Redis 데이터
//    private int score = 0;  //Score는 딱히 필요 없으므로 하나로 통일
//
//    public void addToSortedSet(String value) {    //Redis SortedSet에 추가
//        redisTemplate.opsForZSet().add(key, value, score);
//    }
//
//    public Long findFromSortedSet(String value) {   //Redis SortedSet에서 Value를 찾아 인덱스를 반환
//        return redisTemplate.opsForZSet().rank(key, value);
//    }
//
//    public Set<String> findAllValuesAfterIndexFromSortedSet(Long index) {
//        return redisTemplate.opsForZSet().range(key, index, index + 200);   //전체를 다 불러오기 보다는 200개 정도만 가져와도 자동 완성을 구현하는 데 무리가 없으므로 200개로 rough하게 설정
//    }
//
//    public void removeAllOfSortedSet() {
//        redisTemplate.opsForZSet().removeRange(key, 0, -1);
//    }
//}
