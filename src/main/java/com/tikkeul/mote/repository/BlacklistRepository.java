package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    boolean existsByAdminAndBlackPlate(Admin admin, String blackPlate);

    List<Blacklist> findByAdminOrderByBlackIdAsc(Admin admin);
}
