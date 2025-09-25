package com.tikkeul.mote.service;

import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Blacklist;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.dto.BlacklistPlateResponse;
import com.tikkeul.mote.repository.BlacklistRepository;
import com.tikkeul.mote.repository.ParkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final ParkRepository parkRepository;

    // 차량 번호로 블랙리스트 등록
    public void addByPlate(Admin admin, String plate, String reason) {
        if (admin == null) {
            throw new IllegalArgumentException("관리자 정보가 필요합니다.");
        }

        if (blacklistRepository.existsByAdminAndBlackPlate(admin, plate)) {
            throw new IllegalArgumentException("이미 블랙리스트에 등록된 차량입니다.");
        }

        Blacklist blacklist = Blacklist.builder()
                .admin(admin)
                .blackPlate(plate)
                .reason(reason)
                .blackTimestamp(LocalDateTime.now())
                .build();

        blacklistRepository.save(blacklist);
    }

    // ParkId로 블랙리스트 등록
    public void addFromPark(Admin admin, Long parkId, String reason) {
        if (admin == null) {
            throw new IllegalArgumentException("관리자 정보가 필요합니다.");
        }

        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량이 주차장에 존재하지 않습니다."));

        addByPlate(admin, park.getPlate(), reason);
    }

    // 블랙리스트 조회
    public List<BlacklistPlateResponse> getBlacklist(Admin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("관리자 정보가 필요합니다.");
        }

        return blacklistRepository.findByAdminOrderByBlackIdAsc(admin).stream()
                .map(BlacklistPlateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void updateReason(Admin admin, Long blackId, String newReason) {
        Blacklist blacklist = blacklistRepository.findById(blackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량이 블랙리스트에 존재하지 않습니다."));

        if (!blacklist.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인의 블랙리스트 항목만 수정할 수 있습니다.");
        }

        blacklist.setReason(newReason);
        blacklistRepository.save(blacklist);
    }

    // 블랙리스트 차량 삭제
    public void delete(Admin admin, Long blackId) {
        if (admin == null) {
            throw new IllegalArgumentException("관리자 정보가 필요합니다.");
        }

        Blacklist blacklist = blacklistRepository.findById(blackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량이 블랙리스트에 존재하지 않습니다."));

        if (!blacklist.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인 블랙리스트의 항목만 삭제할 수 있습니다.");
        }

        blacklistRepository.delete(blacklist);
    }

    @Transactional
    public Map<String, Object> deleteAllBlacklist(Admin admin) {
        long deleted = blacklistRepository.deleteByAdmin(admin);
        return Map.of(
                "deleted", deleted
        );
    }

    @Transactional
    public void deleteSelectedBlacklists(Admin admin, List<Long> blacklistIds) {
        List<Blacklist> blacklistsToDelete = blacklistRepository.findAllById(blacklistIds);

        if (blacklistsToDelete.size() != blacklistIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 항목이 포함되어 있습니다.");
        }

        for (Blacklist blacklist : blacklistsToDelete) {
            if (!blacklist.getAdmin().getAdminId().equals(admin.getAdminId())) {
                throw new IllegalStateException("본인 주차장의 항목만 삭제할 수 있습니다.");
            }
        }

        blacklistRepository.deleteAllByIdInBatch(blacklistIds);
    }

    public boolean existsByAdminAndPlate(Admin admin, String plate) {
        return blacklistRepository.existsByAdminAndBlackPlate(admin, plate);
    }
}
