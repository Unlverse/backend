package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.EntryRequest;
import com.tikkeul.mote.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryRequestRepository extends JpaRepository<EntryRequest, Long> {

    List<EntryRequest> findByAdmin(Admin admin);
    long deleteByAdmin(Admin admin);
    boolean existsByAdminAndNewPlate(Admin admin, String newPlate);
}
