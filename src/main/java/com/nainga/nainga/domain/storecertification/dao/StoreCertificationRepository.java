package com.nainga.nainga.domain.storecertification.dao;

import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import jakarta.persistence.EntityManager;
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
}
