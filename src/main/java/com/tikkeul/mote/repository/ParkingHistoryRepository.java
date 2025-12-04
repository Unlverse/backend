package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingHistory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkingHistoryRepository extends JpaRepository<ParkingHistory, Long> {


    List<ParkingHistory> findByAdminAndExitTimeBetweenOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end);
    List<ParkingHistory> findByAdminAndEntryTimeBetweenOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end);

    List<ParkingHistory> findByAdminAndExitTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end, String historyPlate);
    List<ParkingHistory> findByAdminAndEntryTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(Admin admin, LocalDateTime start, LocalDateTime end, String historyPlate);

    List<ParkingHistory> findByAdminAndExitTimeBetween(Admin admin, LocalDateTime start, LocalDateTime end);
    long countByAdminAndEntryTimeBetween(Admin admin, LocalDateTime start, LocalDateTime end);

    //  일별 일반 주차 매출 및 출차 수 (GROUP BY 적용)
    @Query("SELECT FUNCTION('DATE', p.exitTime) as date, SUM(p.fee) as totalFee, COUNT(p) as exitCount " +
            "FROM ParkingHistory p " +
            "WHERE p.admin = :admin AND p.exitTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', p.exitTime)")
    List<Object[]> findDailyExitStats(@Param("admin") Admin admin, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //  일별 입차 수 (ParkingHistory 기준)
    @Query("SELECT FUNCTION('DATE', p.entryTime) as date, COUNT(p) as entryCount " +
            "FROM ParkingHistory p " +
            "WHERE p.admin = :admin AND p.entryTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', p.entryTime)")
    List<Object[]> findDailyEntryStats(@Param("admin") Admin admin, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
