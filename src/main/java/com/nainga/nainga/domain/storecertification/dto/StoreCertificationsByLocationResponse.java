package com.nainga.nainga.domain.storecertification.dto;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.domain.StoreRegularOpeningHours;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreCertificationsByLocationResponse {
    Long id;
    String googlePlaceId;   //Google Map API에서 사용하는 place_id를 저장
    String displayName;    //가게 이름
    String primaryTypeDisplayName;    //가게 업종
    String formattedAddress;    //가게 전체 주소
    String phoneNumber;    //전화번호
    Location location;    //(경도, 위도) 좌표
    List<StoreRegularOpeningHours> regularOpeningHours;   //영업 시간
    List<String> photos;  //사진들
    List<String> certificationName;   //가지고 있는 인증제 이름 리스트

    public StoreCertificationsByLocationResponse(StoreCertification storeCertification) {
        this.id = storeCertification.getStore().getId();
        this.googlePlaceId = storeCertification.getStore().getGooglePlaceId();
        this.displayName = storeCertification.getStore().getDisplayName();
        this.primaryTypeDisplayName = storeCertification.getStore().getPrimaryTypeDisplayName();
        this.formattedAddress = storeCertification.getStore().getFormattedAddress();
        this.phoneNumber = storeCertification.getStore().getPhoneNumber();
        this.location = new Location(storeCertification.getStore().getLocation().getX(), storeCertification.getStore().getLocation().getY());
        this.regularOpeningHours = storeCertification.getStore().getRegularOpeningHours();
        this.photos = storeCertification.getStore().getPhotos();
        this.certificationName = new ArrayList<String>();
        this.certificationName.add(storeCertification.getCertification().getName());
    }
}
