package com.nainga.nainga.domain.storecertification.dao;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

    //북서쪽 좌표와 남동쪽 좌표를 받아 그 두 좌표로 만들어지는 최소 사각형 내에 위치하는 가게들 리턴
    public List<StoreCertification> findStoreCertificationsByLocation(Location northWestLocation, Location southEastLocation) {
        String pointFormat = String.format(
                "'LINESTRING(%f %f, %f %f)'",   //POINT는 (경도, 위도) 순이다. 즉, (Logitude, Latitude)순
                northWestLocation.getLongitude(), northWestLocation.getLatitude(), southEastLocation.getLongitude(), southEastLocation.getLatitude()
        );

        Query query = em.createNativeQuery("SELECT sc.* " + "FROM store_certification AS sc " + "JOIN store AS s ON sc.store_id = s.store_id " + "JOIN certification AS c ON sc.certification_id = c.certification_id " + "WHERE MBRCONTAINS(ST_LINESTRINGFROMTEXT(" + pointFormat + "), s.location)", StoreCertification.class);

        return query.getResultList();
    }
}
