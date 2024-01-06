package com.nainga.nainga.domain.store.dao;

import com.nainga.nainga.domain.store.domain.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepository {

    private final EntityManager em;

    public void save(Store store) {
        em.persist(store);
    }

    public Store findById(Long id) {
        return em.find(Store.class, id);
    }
}
