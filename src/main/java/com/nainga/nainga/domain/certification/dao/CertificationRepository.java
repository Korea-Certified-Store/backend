package com.nainga.nainga.domain.certification.dao;

import com.nainga.nainga.domain.certification.domain.Certification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CertificationRepository {
    private final EntityManager em;

    public void save(Certification certification) {
        em.persist(certification);
    }

    public Optional<Certification> findById(Long id) {
        List<Certification> result = em.createQuery("select c from Certification c where c.id = :id", Certification.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findAny();
    }

    public List<Certification> findAll() {
        return em.createQuery("select c from Certification c", Certification.class)
                .getResultList();
    }
}
