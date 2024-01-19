package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        List<Long> allStoreIds = new ArrayList<>();
        for (StoreCertification storeCertification : allStoreCertifications) {
            allStoreIds.add(storeCertification.getStore().getId());
        }

        List<Long> duplicatedIds = allStoreIds.stream()
                .filter(e -> allStoreIds.indexOf(e) != allStoreIds.lastIndexOf(e))  //중복된 StoreId가 있는 경우
                .distinct() //해당 id를 모아서 1번씩만(중복 제거) 리스트에 담아 전달
                .collect(Collectors.toList());

        return duplicatedIds;
    }
}
