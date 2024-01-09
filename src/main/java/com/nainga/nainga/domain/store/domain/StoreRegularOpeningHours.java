package com.nainga.nainga.domain.store.domain;

import jakarta.persistence.*;
import lombok.Data;


//따라서, Ebeddable type에 대해서 아래처럼 모든 필드에 대한 생성자와 기본 생성자를 만들어줘야한다.
//이처럼 기본 생성자를 만들어주는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 프록시나 리플렉션 같은 기술을 사용할 수 있도록 지원해주기 위함이다.
//public, protected 까지는 사용할 수 있도록 허용해주므로 안전하게 protected로 사용했다.
@Embeddable
@Data
public class StoreRegularOpeningHours {
    @Embedded
    @AttributeOverrides({   //이렇게 AttributeOverride를 안해주면, 같은 Embedded type을 하나의 Entity에서 쓰므로 Column name 중복 오류가 발생한다.
            @AttributeOverride(name = "day", column = @Column(name = "open_day")),
            @AttributeOverride(name = "hour", column = @Column(name = "open_hour")),
            @AttributeOverride(name = "minute", column = @Column(name = "open_minute"))
    })
    private StoreOpenCloseDay open;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "day", column = @Column(name = "close_day")),
            @AttributeOverride(name = "hour", column = @Column(name = "close_hour")),
            @AttributeOverride(name = "minute", column = @Column(name = "close_minute"))
    })
    private StoreOpenCloseDay close;

    public StoreRegularOpeningHours() {
    }

    public StoreRegularOpeningHours(StoreOpenCloseDay open, StoreOpenCloseDay close) {
        this.open = open;
        this.close = close;
    }
}
