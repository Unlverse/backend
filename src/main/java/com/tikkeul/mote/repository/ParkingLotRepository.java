package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingLot;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    Optional<ParkingLot> findByParkingLotName(String parkingLotName);
    List<ParkingLot> findByAdmin(Admin admin);

    @Query(
            value = """
        SELECT 
           pl.admin_id               AS adminId,
           pl.parking_lot_name       AS name,
           pl.address                AS address,
           pl.parking_lot_latitude   AS latitude,
           pl.parking_lot_longitude  AS longitude,
           (6371 * acos(LEAST(1,
               cos(radians(:lat)) * cos(radians(pl.parking_lot_latitude)) *
               cos(radians(pl.parking_lot_longitude) - radians(:lng)) +
               sin(radians(:lat)) * sin(radians(pl.parking_lot_latitude))
           )))                       AS distanceKm
        FROM parking_lot pl
        WHERE pl.parking_lot_latitude BETWEEN :lat - (:radiusKm/111.0) AND :lat + (:radiusKm/111.0)
          AND pl.parking_lot_longitude BETWEEN :lng - (:radiusKm/111.0/cos(radians(:lat))) AND :lng + (:radiusKm/111.0/cos(radians(:lat)))
        HAVING distanceKm <= :radiusKm
        ORDER BY distanceKm ASC
      """,
            nativeQuery = true
    )

    List<Object[]> findNearbyLotsRaw(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm
    );


}

