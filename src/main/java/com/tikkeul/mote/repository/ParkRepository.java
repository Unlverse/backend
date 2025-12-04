package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    // 일별 입차 수 (현재 주차중인 Park 테이블 기준)
    @Query("SELECT FUNCTION('DATE', p.timestamp) as date, COUNT(p) as entryCount " +
            "FROM Park p " +
            "WHERE p.admin = :admin AND p.timestamp BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', p.timestamp)")
    List<Object[]> findDailyParkEntryStats(@Param("admin") Admin admin, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
