package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StoreCertificationService {
    private final StoreCertificationRepository storeCertificationRepository;
    private List<Long> duplicatedStoreIds;  //여러 인증제를 가지는 중복된 storeId를 담고있는 리스트

    @Autowired
    public StoreCertificationService(StoreCertificationRepository storeCertificationRepository) {
        this.storeCertificationRepository = storeCertificationRepository;
    }

    @PostConstruct
    public void init() {    //이 Service Bean이 생성된 이후에 한번만 중복된 storeId를 검사해서 Globally하게 저장
        List<StoreCertification> allStoreCertifications = storeCertificationRepository.findAll();   //중복된 id를 검사하기 위함

        HashSet<Long> uniqueStoreIds = new HashSet<>(); //조회 성능을 높이기 위해 HashSet으로 저장
        HashSet<Long> duplicatedIds = new HashSet<>();

        for (StoreCertification storeCertification : allStoreCertifications) {
            Long storeId = storeCertification.getStore().getId();
            if (!uniqueStoreIds.add(storeId)) { //HashSet에 add를 했을 때 이미 존재하는 데이터면 false가 리턴되는 것을 활용
                duplicatedIds.add(storeId);
            }
        }
        duplicatedStoreIds = new ArrayList<>(duplicatedIds);
    }

    public List<StoreCertification> findStoreCertificationsByLocation(Location northWestLocation, Location southWestLocation, Location southEastLocation, Location northEastLocation) {
        return storeCertificationRepository.findStoreCertificationsByLocation(northWestLocation, southWestLocation, southEastLocation, northEastLocation);
    }

    public List<Long> getDuplicatedStoreIds() {
        return duplicatedStoreIds;
    }
}
