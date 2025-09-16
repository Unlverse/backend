package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkingHistoryRepository extends JpaRepository<ParkingHistory, Long> {


    List<ParkingHistory> findByAdminAndExitTimeBetweenOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end);
    List<ParkingHistory> findByAdminAndEntryTimeBetweenOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end);

    List<ParkingHistory> findByAdminAndExitTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end, String historyPlate);
    List<ParkingHistory> findByAdminAndEntryTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end, String historyPlate);
}
