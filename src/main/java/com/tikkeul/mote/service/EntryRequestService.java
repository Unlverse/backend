package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.EntryRequestResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.EntryRequest;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.EntryRequestRepository;
import com.tikkeul.mote.repository.AdminRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryRequestService {

    private final EntryRequestRepository entryRequestRepository;
    private final ParkingLotRepository parkingLotRepository;

    public EntryRequest createRequest(String parkingLotName, String newPlate) {
        ParkingLot parkingLot = parkingLotRepository.findByParkingLotName(parkingLotName)
                .orElseThrow(() -> new IllegalArgumentException("해당 주차장을 찾을 수 없습니다."));

        Admin admin = parkingLot.getAdmin();

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
                    List<ParkingLot> parkingLots = parkingLotRepository.findByAdmin(entryRequest.getAdmin());
                    String parkingLotName = parkingLots.isEmpty() ? "알 수 없음" : parkingLots.get(0).getParkingLotName();

                    return EntryRequestResponse.fromEntity(entryRequest, parkingLotName);
                })
                .toList();
    }

    public void deleteRequest(Long requestId, Admin admin) {
        EntryRequest entryRequest = entryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        if (!entryRequest.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new IllegalStateException("본인 주차장 요청만 삭제할 수 있습니다.");
        }

        entryRequestRepository.delete(entryRequest);
    }
}
