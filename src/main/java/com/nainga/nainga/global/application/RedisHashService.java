package com.nainga.nainga.global.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class RedisHashService {
    private final HashOperations<String, String, String> hashOperations;


    public RedisHashService(RedisTemplate<String, String> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    private String key = "autocorrect"; //검색어 자동 완성을 위한 Redis 데이터

    //Hash에 field-value 쌍을 추가하는 메서드
    public void addToHash(String key, String field, String value) {
        hashOperations.put(key, field, value);
    }

    public Set<String> findAllValuesContainingSearchKeyword(String searchKeyword) {
        //Redis에서는 case insensitive한 검색을 지원하는 내장 모듈이 없으므로 searchKeyword는 모두 소문자로 통일하여 검색하도록 구현
        //당연히 초기 Redis에 field를 저장할 때도 모두 소문자로 변형하여 저장했고 원본 문자열은 value에 저장!
        Set<String> result = new TreeSet<>();   //searchKeyword를 포함하는 원래 가게 이름들의 리스트. 최대 maxSize개까지 저장. 중복 허용하지 않고, 자동 사전순 정렬하기 위해 사용
        final int maxSize = 10;   //최대 검색어 자동 완성 개수

        ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + searchKeyword + "*").build();   //searchKeyword를 포함하는지를 검사하기 위한 scanOption
        Cursor<Map.Entry<String, String>> cursor = hashOperations.scan(key, scanOptions);   //기존 Redis Keys 로직의 성능 이슈를 해결하기 위해 10개 단위로 끊어서 조회하는 Scan 기능 사용

        while (cursor.hasNext()) {  //끊어서 조회하다보니 while loop로 조회
            Map.Entry<String, String> entry = cursor.next();
            result.add(entry.getValue());

            if(result.size() >= maxSize)    //maxSize에 도달하면 scan 중단
                break;
        }
        cursor.close();
        return result;
    }
}
