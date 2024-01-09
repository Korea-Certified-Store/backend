package com.nainga.nainga.domain.store.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class StoreRegularOpeningHours {
    private StoreOpenCloseDay open;
    private StoreOpenCloseDay close;

    public StoreRegularOpeningHours() {
    }

    public StoreRegularOpeningHours(StoreOpenCloseDay open, StoreOpenCloseDay close) {
        this.open = open;
        this.close = close;
    }
}
