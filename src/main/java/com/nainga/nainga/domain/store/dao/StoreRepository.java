package com.nainga.nainga.domain.store.dao;

import com.nainga.nainga.domain.store.domain.Location;
import com.nainga.nainga.domain.store.domain.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

    public Optional<Store> findByDisplayName(String displayName) {
        List<Store> result = em.createQuery("select s from Store s where s.displayName = :displayName", Store.class)
                .setParameter("displayName", displayName)
                .getResultList();
        return result.stream().findAny();
    }

    public Optional<Store> findByGooglePlaceId(String googlePlaceId) {
        List<Store> result = em.createQuery("select s from Store s where s.googlePlaceId = :googlePlaceId", Store.class)
                .setParameter("googlePlaceId", googlePlaceId)
                .getResultList();
        return result.stream().findAny();
    }

    public List<Store> findAll() {
        return em.createQuery("select s from Store s", Store.class)
                .getResultList();
    }

    //북서쪽 좌표와 남동쪽 좌표를 받아 그 두 좌표로 만들어지는 최소 사각형 내에 위치하는 가게들 리턴
    public List<Store> findStoresByLocation(Location northWestLocation, Location southEastLocation) {
        String pointFormat = String.format(
                "'LINESTRING(%f %f, %f %f)'",   //POINT는 (경도, 위도) 순이다. 즉, (Logitude, Latitude)순
                northWestLocation.getLongitude(), northWestLocation.getLatitude(), southEastLocation.getLongitude(), southEastLocation.getLatitude()
        );

        Query query = em.createNativeQuery("SELECT * " + "FROM store AS s " + "WHERE MBRCONTAINS(ST_LINESTRINGFROMTEXT(" + pointFormat + "), s.location)", Store.class);

        return query.getResultList();
    }
}
