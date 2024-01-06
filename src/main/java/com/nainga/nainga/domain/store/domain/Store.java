package com.nainga.nainga.domain.store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Entity
@Getter
@Builder    //필드 명시를 통한 가독성 향상을 위해 생성자 대신 빌더 패턴을 적용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Store {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Hibernate 버전 5이상이 되면서 @GeneratedValue의 Defualt strategy인 Auto가 MySQL에 대해 Table 전략을 사용하므로
    //이를 보편적인 IDENTITY로 명시적으로 변환 시켜주기 위함. Table 전략을 사용하게 되면 Sequence를 모방하기 위해 seq 테이블이 별도로 추가 생성됨.
    @Column(name = "store_id")
    private Long id;
    private String googlePlaceId;   //Google Map API에서 사용하는 place_id를 저장
    private String displayName;    //가게 이름
    private String primaryType;    //가게 업종
    private String formattedAddress;    //가게 전체 주소
    private String internationalPhoneNumber;    //국제 전화번호
    @Column(columnDefinition = "GEOMETRY")
    private Point location;    //(위도, 경도) 좌표
    @ElementCollection
    private List<String> regularOpeningHours;   //영업 시간
    @ElementCollection
    private List<String> photos;    //가게 사진들
}
