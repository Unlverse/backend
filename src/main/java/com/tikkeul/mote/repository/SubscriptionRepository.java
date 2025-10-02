package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByAdmin(Admin admin);
    Optional<Subscription> findByAdminAndSubPlate(Admin admin, String subPlate);
    boolean existsByAdminAndSubPlateAndEndDateAfter(Admin admin, String subPlate, LocalDate date);
    long deleteByAdmin(Admin admin);
    long countByAdmin(Admin admin);
    void deleteByEndDateBefore(LocalDate date);
}
