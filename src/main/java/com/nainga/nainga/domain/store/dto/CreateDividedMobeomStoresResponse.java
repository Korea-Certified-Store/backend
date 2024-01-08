package com.nainga.nainga.domain.store.dto;

import lombok.Data;

@Data
public class CreateDividedMobeomStoresResponse {
    private Double dollars;   //API Call하고 남은 달러 리턴
    private int nextIndex;  //조회가 모두 끝났으면 -1 리턴, 아직 안끝났으면 다음에 시작해야할 인덱스 리턴
}
