package com.nainga.nainga.domain.report.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@DiscriminatorValue("new")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewStoreReport extends Report {
    private String storeName;   //가게 이름
    private String formattedAddress;    //가게 주소
    @ElementCollection
    private List<String> certifications;    //가게가 가지고 있는 인증제 이름 리스트
}
