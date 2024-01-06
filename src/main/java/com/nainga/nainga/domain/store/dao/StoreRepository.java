package com.nainga.nainga.domain.store.dao;

import com.nainga.nainga.domain.store.domain.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoreRepository {

    private final EntityManager em;

    public void save(Store store) {
        em.persist(store);
    }

    public Optional<Store> findById(Long id) {
        List<Store> result = em.createQuery("select s from Store s where s.id = :id", Store.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findAny();
    }

    public Optional<Store> findByGooglePlaceId(String googlePlaceId) {
        List<Store> result = em.createQuery("select s from Store s where s.googlePlaceId = :googlePlaceId", Store.class)
                .setParameter("googlePlaceId", googlePlaceId)
                .getResultList();
        return result.stream().findAny();
    }
}
