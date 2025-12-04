package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.SubscriptionHistory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    List<SubscriptionHistory> findByAdminAndHistoryStartDateBetween(Admin admin, LocalDate start, LocalDate end);
    List<SubscriptionHistory> findByAdminAndHistoryStartDateBetweenOrderByHistoryStartDateDesc(Admin admin, LocalDate start, LocalDate end);
    List<SubscriptionHistory> findByAdminAndHistoryStartDateBetweenAndSubHistoryPlateContainingOrderByHistoryStartDateDesc(Admin admin, LocalDate start, LocalDate end, String plateKeyword);
    Optional<SubscriptionHistory> findByAdminAndSubHistoryPlateAndHistoryStartDate(Admin admin, String plate, LocalDate startDate);
    List<SubscriptionHistory> findByAdminAndRefundedAtBetween(Admin admin, LocalDateTime start, LocalDateTime end);

    // 일별 정기권 매출 (시작일 기준 GROUP BY)
    @Query("SELECT s.historyStartDate as date, SUM(s.subHistoryPrice) as totalSales " +
            "FROM SubscriptionHistory s " +
            "WHERE s.admin = :admin AND s.historyStartDate BETWEEN :start AND :end " +
            "GROUP BY s.historyStartDate")
    List<Object[]> findDailySubscriptionStats(@Param("admin") Admin admin, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 일별 정기권 환불 (환불일 기준 GROUP BY)
    @Query("SELECT FUNCTION('DATE', s.refundedAt) as date, SUM(s.refundAmount) as totalRefund " +
            "FROM SubscriptionHistory s " +
            "WHERE s.admin = :admin AND s.refundedAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', s.refundedAt)")
    List<Object[]> findDailyRefundStats(@Param("admin") Admin admin, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}