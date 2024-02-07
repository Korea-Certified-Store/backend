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
}

