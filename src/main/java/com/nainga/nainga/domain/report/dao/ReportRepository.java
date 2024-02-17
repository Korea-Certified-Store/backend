package com.nainga.nainga.domain.report.dao;

import com.nainga.nainga.domain.report.domain.Report;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final EntityManager em;

    public void save(Report report) {
        em.persist(report);
    }

    public Optional<Report> findById(Long id) {
        List<Report> result = em.createQuery("select r from Report r where r.id = :id", Report.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findAny();
    }

    public List<Report> findAll() {
        return em.createQuery("select r from Report r", Report.class)
                .getResultList();
    }
}
