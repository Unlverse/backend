package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.EntryRequestResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.EntryRequest;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.EntryRequestRepository;
import com.tikkeul.mote.repository.AdminRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntryRequestService {

    private final EntryRequestRepository entryRequestRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final AdminRepository adminRepository;
    private final ParkService parkService;

    public EntryRequest createRequest(Long adminId, String newPlate) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다."));

        if (entryRequestRepository.existsByAdminAndNewPlate(admin, newPlate)) {
            throw new IllegalStateException("이미 처리 대기 중인 입차 요청입니다.");
        }

        EntryRequest request = EntryRequest.builder()
                .admin(admin)
                .newPlate(newPlate)
                .build();

        return entryRequestRepository.save(request);
    }

    public List<EntryRequestResponse> getRequestsForAdmin(Admin admin) {
        List<EntryRequest> requests = entryRequestRepository.findByAdmin(admin);

        return requests.stream()
                .map(entryRequest -> {
                    String parkingLotName = parkingLotRepository.findByAdmin(entryRequest.getAdmin())
                            .map(ParkingLot::getParkingLotName)
                            .orElse("알 수 없음");

                    return EntryRequestResponse.fromEntity(entryRequest, parkingLotName);
                })
                .toList();
    }

    @Transactional
    public void acceptRequest(Long requestId, Admin admin) {
        EntryRequest entryRequest = entryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 입차 요청입니다."));

        if (!entryRequest.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new IllegalStateException("본인 주차장만 처리할 수 있습니다.");
        }

        // ParkService를 호출하여 입차 처리 (force=false)
        parkService.manualSavePark(admin, entryRequest.getNewPlate(), false);

        // 성공 시 요청 삭제
        entryRequestRepository.delete(entryRequest);
    }

    @Transactional
    public void acceptAllRequests(Admin admin) {
        List<EntryRequest> allRequests = entryRequestRepository.findByAdmin(admin);

        for (EntryRequest request : allRequests) {
            parkService.manualSavePark(admin, request.getNewPlate(), false);
        }

        entryRequestRepository.deleteAll(allRequests);
    }

    @Transactional
    public void acceptSelectedRequests(Admin admin, List<Long> requestIds) {
        List<EntryRequest> selectedRequests = entryRequestRepository.findAllById(requestIds);

        if (selectedRequests.size() != requestIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 입차 요청이 포함되어 있습니다.");
        }

        for (EntryRequest request : selectedRequests) {
            if (!request.getAdmin().getAdminId().equals(admin.getAdminId())) {
                throw new IllegalStateException("본인 주차장만 처리할 수 있습니다.");
            }
            parkService.manualSavePark(admin, request.getNewPlate(), false);
        }

        entryRequestRepository.deleteAll(selectedRequests);
    }
}
