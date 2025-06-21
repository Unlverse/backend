package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.ParkingInfoRequest;
import com.tikkeul.mote.dto.ParkingInfoResponse;
import com.tikkeul.mote.service.ParkingInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingInfoController {

    private final ParkingInfoService parkingInfoService;

    @PostMapping("/info")
    public ResponseEntity<ParkingInfoResponse> getParkingInfo(@RequestBody ParkingInfoRequest request) {
        ParkingInfoResponse response = parkingInfoService.getParkingInfo(request.getCarNumber());
        return ResponseEntity.ok(response);
    }
}
