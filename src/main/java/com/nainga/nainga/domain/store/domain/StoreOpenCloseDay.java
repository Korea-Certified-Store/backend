package com.nainga.nainga.domain.store.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class StoreOpenCloseDay {
    private StoreDay day;
    private int hour;
    private int minute;

}
