package com.nainga.nainga.domain.report.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@DiscriminatorValue("fix")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixStoreReport extends Report {
    private Long storeId;   //가게 id
    private String contents;    //신고 내용
}
