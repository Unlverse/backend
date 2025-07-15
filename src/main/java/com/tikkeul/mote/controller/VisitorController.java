package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.VisitorNotFoundRequest;
import com.tikkeul.mote.dto.VisitorNotFoundResponse;
import com.tikkeul.mote.dto.VisitorParkInfoRequest;
import com.tikkeul.mote.dto.VisitorParkInfoResponse;
import com.tikkeul.mote.service.ParkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final ParkService parkService;

    @PostMapping("/park-info")
    public ResponseEntity<VisitorParkInfoResponse> getParkInfo(
            @RequestBody VisitorParkInfoRequest request) {
        VisitorParkInfoResponse response = parkService.getParkInfoByPlate(request.getPlate());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/not-found-info")
    public ResponseEntity<List<VisitorNotFoundResponse>> getNotFoundParksByName(
            @RequestBody VisitorNotFoundRequest request) {
        return ResponseEntity.ok(parkService.getNotFoundByParkingLotName(request.getParkingLotName()));
    }

    @GetMapping("/park-info/{parkId}")
    public ResponseEntity<VisitorParkInfoResponse> getParkInfoById(
            @PathVariable("parkId") Long parkId) {
        VisitorParkInfoResponse response = parkService.getParkInfoById(parkId);
        return ResponseEntity.ok(response);
    }
}
