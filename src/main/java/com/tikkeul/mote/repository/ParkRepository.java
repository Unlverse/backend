package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkRepository extends JpaRepository<Park, Long> {
    Optional<Park> findByPlate(String plate);
    List<Park> findByAdmin(Admin admin);
    boolean existsByPlate(String plate);
    long countByAdmin(Admin admin);

    List<Park> findByAdminAndPlate(Admin admin, String plate);
    long deleteByAdmin(Admin admin);

    long countByAdminAndTimestampBetween(Admin admin, LocalDateTime start, LocalDateTime end);
}
