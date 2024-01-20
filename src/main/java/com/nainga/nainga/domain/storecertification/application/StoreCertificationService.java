package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreCertificationService {
    private final StoreCertificationRepository storeCertificationRepository;
    public List<StoreCertification> findStoreCertificationsByLocation(Location northWestLocation, Location southWestLocation, Location southEastLocation, Location northEastLocation) {
        return storeCertificationRepository.findStoreCertificationsByLocation(northWestLocation, southWestLocation, southEastLocation, northEastLocation);
    }

    public List<Long> findStoreIdsWithMultipleCertifications() {
        List<StoreCertification> allStoreCertifications = storeCertificationRepository.findAll();   //중복된 id를 검사하기 위함

        HashSet<Long> uniqueStoreIds = new HashSet<>(); //조회 성능을 높이기 위해 HashSet으로 저장
        HashSet<Long> duplicatedStoreIds = new HashSet<>();

        for (StoreCertification storeCertification : allStoreCertifications) {
            Long storeId = storeCertification.getStore().getId();
            if (!uniqueStoreIds.add(storeId)) { //HashSet에 add를 했을 때 이미 존재하는 데이터면 false가 리턴되는 것을 활용
                duplicatedStoreIds.add(storeId);
            }
        }
        return new ArrayList<>(duplicatedStoreIds);
    }
}
