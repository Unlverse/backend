package com.tikkeul.mote.repository;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkRepository extends JpaRepository<Park, Long> {
    Optional<Park> findTopByPlateOrderByTimestampDesc(String plate);

    //  관리자 기준 차량 수 세기
    long countByAdmin(Admin admin);

    //  관리자 기준 등록된 차량 목록 조회
    List<Park> findByAdmin(Admin admin);
}
