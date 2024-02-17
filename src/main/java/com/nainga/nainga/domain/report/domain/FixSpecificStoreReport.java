package com.nainga.nainga.domain.report.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Builder
@DiscriminatorValue("fix")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixSpecificStoreReport extends Report {
    private Long storeId;   //가게 id
    private String contents;    //신고 내용
}
