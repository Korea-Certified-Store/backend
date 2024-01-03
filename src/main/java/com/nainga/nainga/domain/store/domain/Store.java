package com.nainga.nainga.domain.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder    //필드 명시를 통한 가독성 향상을 위해 생성자 대신 빌더 패턴을 적용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Store {

    @Id @GeneratedValue
    @Column(name = "store_id")
    private Long id;
    private String name;    //가게 이름
    private String primaryType;    //가게 업종
    private String formattedAddress;    //가게 전체 주소
    private String regularOpeningHours;    //영업 시간
    private String internationalPhoneNumber;    //국제 전화번호
//    private String location;    //(위도, 경도) 좌표
//    private String photos;    //가게 사진들
}
