package com.nainga.nainga.domain.storecertification.dao;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoreCertificationRepository {
    private final EntityManager em;

    public void save(StoreCertification storeCertification) {
        em.persist(storeCertification);
    }

    public Optional<StoreCertification> findById(Long id) {
        List<StoreCertification> result = em.createQuery("select sc from StoreCertification sc where sc.id = :id", StoreCertification.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findAny();
    }

    public List<StoreCertification> findAll() {
        return em.createQuery("select sc from StoreCertification sc", StoreCertification.class)
                .getResultList();
    }

    public Optional<StoreCertification> findByStoreIdCertificationId(Long storeId, Long certificationId) {
        List<StoreCertification> result = em.createQuery("select sc from StoreCertification sc where sc.store.id = :storeId and sc.certification.id = :certificationId", StoreCertification.class)
                .setParameter("storeId", storeId)
                .setParameter("certificationId", certificationId)
                .getResultList();
        return result.stream().findAny();
    }

    //북서쪽 좌표, 남서쪽 좌표, 남동쪽 좌표, 북동쪽 좌표를 받아 그 네 좌표로 만들어지는 사각형 영역 내에 위치하는 가게들 리턴
    public List<StoreCertification> findStoreCertificationsByLocation(Location northWestLocation, Location southWestLocation, Location southEastLocation, Location northEastLocation) {
        String pointFormat = String.format(
                "'POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))'",   //POINT는 (경도, 위도) 순이다. 즉, (Logitude, Latitude)순
                northWestLocation.getLongitude(), northWestLocation.getLatitude(), southWestLocation.getLongitude(), southWestLocation.getLatitude(), southEastLocation.getLongitude(), southEastLocation.getLatitude(), northEastLocation.getLongitude(), northEastLocation.getLatitude(), northWestLocation.getLongitude(), northWestLocation.getLatitude()
        );

        Query query = em.createNativeQuery("SELECT sc.* " + "FROM store_certification AS sc " + "JOIN store AS s ON sc.store_id = s.store_id " + "JOIN certification AS c ON sc.certification_id = c.certification_id " + "WHERE ST_CONTAINS(ST_POLYGONFROMTEXT(" + pointFormat + "), s.location)", StoreCertification.class);

        return query.getResultList();
    }

    //북서쪽 좌표, 남서쪽 좌표, 남동쪽 좌표, 북동쪽 좌표를 받아 그 네 좌표로 만들어지는 사각형 영역 내에 위치하는 가게들 리턴
    public List<StoreCertification> findStoreCertificationsByLocationRandomly(Location northWestLocation, Location southWestLocation, Location southEastLocation, Location northEastLocation) {
        String pointFormat = String.format(
                "'POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))'",   //POINT는 (경도, 위도) 순이다. 즉, (Logitude, Latitude)순
                northWestLocation.getLongitude(), northWestLocation.getLatitude(), southWestLocation.getLongitude(), southWestLocation.getLatitude(), southEastLocation.getLongitude(), southEastLocation.getLatitude(), northEastLocation.getLongitude(), northEastLocation.getLatitude(), northWestLocation.getLongitude(), northWestLocation.getLatitude()
        );

        TypedQuery<StoreCertification> query = em.createQuery(
                "SELECT sc FROM StoreCertification sc " +
                        "JOIN FETCH sc.store s " +
                        "JOIN FETCH sc.certification c " +
                        "WHERE ST_CONTAINS(ST_POLYGONFROMTEXT(" + pointFormat + "), s.location) ORDER BY RAND() LIMIT 225",
                StoreCertification.class);

        return query.getResultList();
    }

    public List<StoreCertification> findStoreCertificationsByStoreId(Long storeId) {    //storeId를 통해 관련된 모든 StoreCertification 조회
        TypedQuery<StoreCertification> query = em.createQuery(
                "SELECT sc FROM StoreCertification sc " +
                        "JOIN FETCH sc.certification c " +
                        "WHERE sc.store.id = :storeId", StoreCertification.class
        ).setParameter("storeId", storeId);

        return query.getResultList();
    }

    //검색어를 이용해 가게 이름, 업종, 주소에 대해 검색하고 나온 검색 결과 중 사용자로부터 가까운 순으로 최대 75개의 가게 정보를 리턴
    public List<StoreCertification> searchStoreCertificationsByLocation(Location currentLocation, String searchKeyword) { //사용자의 현재 (경도, 위도) 좌표와 검색 키워드를 전달 받기
        TypedQuery<StoreCertification> query = em.createQuery(
                "SELECT sc FROM StoreCertification sc " +
                        "JOIN FETCH sc.store s " +
                        "JOIN FETCH sc.certification c " +
                        "WHERE sc.store.displayName LIKE CONCAT('%', :searchKeyword, '%') " +   //가게 이름에 대한 검색
                        "OR sc.store.primaryTypeDisplayName LIKE CONCAT('%', :searchKeyword, '%') " +   //업종에 대한 검색
                        "OR sc.store.formattedAddress LIKE CONCAT('%', :searchKeyword, '%') " + //주소에 대한 검색
                        "ORDER BY (6371 * acos(cos(radians(36.628486474734)) * cos(radians(ST_Y(:currentLocation))) * cos(radians(ST_X(:currentLocation)) - radians(127.4574415007155)) + sin(radians(36.628486474734)) * sin(radians(ST_Y(:currentLocation))))) LIMIT 75", //하버사인 공식을 이용해 두 좌표상 거리 구하기
                StoreCertification.class).setParameter("currentLocation", currentLocation)
                .setParameter("searchKeyword", searchKeyword);

        return query.getResultList();
    }
}

