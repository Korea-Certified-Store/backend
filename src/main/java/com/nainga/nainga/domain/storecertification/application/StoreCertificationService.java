package com.nainga.nainga.domain.storecertification.application;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.domain.storecertification.dto.StoreCertificationsByLocationResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public List<List<StoreCertificationsByLocationResponse>> findStoreCertificationsByLocationRandomly(Location northWestLocation, Location southWestLocation, Location southEastLocation, Location northEastLocation) {
        List<StoreCertification> storeCertificationsByLocation = storeCertificationRepository.findStoreCertificationsByLocationRandomly(northWestLocation, southWestLocation, southEastLocation, northEastLocation);
        List<StoreCertificationsByLocationResponse> storeCertificationsByLocationResponses = new ArrayList<>(); //반환해줄 StoreCertificationsByLocationResponse들의 List

        Map<Long, Boolean> isChecked = new HashMap<>(); //이미 조회한 가게인지 여부를 저장하는 HashMap

        for (StoreCertification storeCertification : storeCertificationsByLocation) {
            if (!duplicatedStoreIds.contains(storeCertification.getStore().getId())) {
                storeCertificationsByLocationResponses.add(new StoreCertificationsByLocationResponse(storeCertification));
            } else {
                Boolean result = isChecked.get(storeCertification.getStore().getId());
                if (result == null) {
                    StoreCertificationsByLocationResponse storeCertificationsByLocationResponse = new StoreCertificationsByLocationResponse(storeCertification);

                    List<StoreCertification> storeCertificationsByStoreId = storeCertificationRepository.findStoreCertificationsByStoreId(storeCertification.getStore().getId());
                    for (StoreCertification storeCertificationByStoreId : storeCertificationsByStoreId) {   //위에서 이미 추가해준 인증제 이름일 경우 제외
                        if(!storeCertificationByStoreId.getCertification().getName().equals(storeCertification.getCertification().getName()))
                            storeCertificationsByLocationResponse.getCertificationName().add(storeCertificationByStoreId.getCertification().getName());
                    }

                    storeCertificationsByLocationResponses.add(storeCertificationsByLocationResponse);
                    isChecked.put(storeCertification.getStore().getId(), true); //체크되었다고 기록
                }
            }
        }

        List<List<StoreCertificationsByLocationResponse>> storeCertificationsByLocationListResponses = new ArrayList<>();

        List<StoreCertificationsByLocationResponse> subArray = new ArrayList<>();
        for(int i=1; i <= storeCertificationsByLocationResponses.size(); ++i) {
            subArray.add(storeCertificationsByLocationResponses.get(i-1));

            if (i == 75 || i == storeCertificationsByLocationResponses.size()) { //요구사항에 따른 가게 최대 개수가 75개 이므로, 0부터 74까지만!
                storeCertificationsByLocationListResponses.add(subArray);
                break;
            }

            if (i % 15 == 0) {  //15개씩 1회차를 나눠주기 위해
                storeCertificationsByLocationListResponses.add(subArray);
                subArray = new ArrayList<>();
            }
        }

        return storeCertificationsByLocationListResponses;
    }

    //검색어를 이용해 가게 이름, 업종, 주소에 대해 검색하고 나온 검색 결과 중 사용자로부터 가까운 순으로 최대 75개의 가게 정보를 리턴
    //단 이 로직은 UX를 고려해서 총 75개의 가게를 15개씩 쪼개서 5회차로 나누어 보내는 로직은 구현하지 않기로 결정
    public List<StoreCertificationsByLocationResponse> searchStoreCertificationsByLocationAndKeyword(Double currLong, Double currLat, String searchKeyword) {
        List<StoreCertification> storeCertificationsByLocation = storeCertificationRepository.searchStoreCertificationsByLocationAndKeyword(currLong, currLat, searchKeyword);
        System.out.println("storeCertificationsByLocation.size() = " + storeCertificationsByLocation.size());
        List<StoreCertificationsByLocationResponse> storeCertificationsByLocationResponses = new ArrayList<>(); //반환해줄 StoreCertificationsByLocationResponse들의 List

        Map<Long, Boolean> isChecked = new HashMap<>(); //이미 조회한 가게인지 여부를 저장하는 HashMap

        for (StoreCertification storeCertification : storeCertificationsByLocation) {
            if (!duplicatedStoreIds.contains(storeCertification.getStore().getId())) {
                storeCertificationsByLocationResponses.add(new StoreCertificationsByLocationResponse(storeCertification));
            } else {
                Boolean result = isChecked.get(storeCertification.getStore().getId());
                if (result == null) {
                    StoreCertificationsByLocationResponse storeCertificationsByLocationResponse = new StoreCertificationsByLocationResponse(storeCertification);

                    List<StoreCertification> storeCertificationsByStoreId = storeCertificationRepository.findStoreCertificationsByStoreId(storeCertification.getStore().getId());
                    for (StoreCertification storeCertificationByStoreId : storeCertificationsByStoreId) {   //위에서 이미 추가해준 인증제 이름일 경우 제외
                        if(!storeCertificationByStoreId.getCertification().getName().equals(storeCertification.getCertification().getName()))
                            storeCertificationsByLocationResponse.getCertificationName().add(storeCertificationByStoreId.getCertification().getName());
                    }

                    storeCertificationsByLocationResponses.add(storeCertificationsByLocationResponse);
                    isChecked.put(storeCertification.getStore().getId(), true); //체크되었다고 기록
                }
            }
        }

        return storeCertificationsByLocationResponses;
    }

    public List<Long> getDuplicatedStoreIds() {
        return duplicatedStoreIds;
    }
}
