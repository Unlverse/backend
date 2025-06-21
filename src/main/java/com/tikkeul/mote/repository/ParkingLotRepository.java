package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    Optional<ParkingLot> findByAdmin(Admin admin);
    Optional<ParkingLot> findByAdminId(Long adminId);

}