package com.nainga.nainga.domain.store.dto;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.domain.StoreRegularOpeningHours;
import lombok.Data;

import java.util.List;

@Data
public class StoresByLocationResponse {
    Long id;
    String googlePlaceId;   //Google Map API에서 사용하는 place_id를 저장
    String displayName;    //가게 이름
    String primaryTypeDisplayName;    //가게 업종
    String formattedAddress;    //가게 전체 주소
    String phoneNumber;    //전화번호
    Location location;    //(경도, 위도) 좌표
    List<StoreRegularOpeningHours> regularOpeningHours;   //영업 시간
    List<String> photos;  //사진들

    public StoresByLocationResponse(Store store) {
        this.id = store.getId();
        this.googlePlaceId = store.getGooglePlaceId();
        this.displayName = store.getDisplayName();
        this.primaryTypeDisplayName = store.getPrimaryTypeDisplayName();
        this.formattedAddress = store.getFormattedAddress();
        this.phoneNumber = store.getPhoneNumber();
        this.location = new Location(store.getLocation().getX(), store.getLocation().getY());
        this.regularOpeningHours = store.getRegularOpeningHours();
        this.photos = store.getPhotos();
    }
}
