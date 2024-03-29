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

    //검색어를 이용해 가게 이름, 업종, 주소에 대해 검색하고 나온 검색 결과 중 사용자로부터 가까운 순으로 최대 30개의 가게 정보를 리턴
    public List<StoreCertification> searchStoreCertificationsByLocationAndKeyword(Double currLong, Double currLat, String searchKeyword) {
        String nativeQuery = "SELECT sc.* FROM store_certification sc " +
                "JOIN store AS s ON sc.store_id = s.store_id " +
                "JOIN certification AS c ON sc.certification_id = c.certification_id " +
                "WHERE s.display_name LIKE :searchKeyword " +
                "OR s.primary_type_display_name LIKE :searchKeyword " +
                "OR s.formatted_address LIKE :searchKeyword " +
                "ORDER BY (6371 * acos(cos(radians(:currLat)) * cos(radians(ST_Y(s.location))) * cos(radians(ST_X(s.location)) - radians(:currLong)) + sin(radians(:currLat)) * sin(radians(ST_Y(s.location))))) limit 90";    //세 인증제를 모두 가지고 있는 가게가 있으므로 3 * 30 = 90

        Query query = em.createNativeQuery(nativeQuery, StoreCertification.class)
                .setParameter("currLong", currLong)
                .setParameter("currLat", currLat)
                .setParameter("searchKeyword", "%" + searchKeyword + "%");

        return query.getResultList();
    }

}

