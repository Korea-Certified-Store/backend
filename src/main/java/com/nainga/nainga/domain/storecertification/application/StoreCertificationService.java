package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreCertificationService {
    private final StoreCertificationRepository storeCertificationRepository;
    public List<StoreCertification> findStoreCertificationsByLocation(Location northWestLocation, Location southEastLocation) {
        return storeCertificationRepository.findStoreCertificationsByLocation(northWestLocation, southEastLocation);
    }
}
