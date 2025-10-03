package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
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
}