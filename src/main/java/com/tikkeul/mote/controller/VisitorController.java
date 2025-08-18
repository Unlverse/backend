package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.*;
import com.tikkeul.mote.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping("/park-info")
    public ResponseEntity<VisitorParkInfoResponse> getParkInfo(
            @RequestBody VisitorParkInfoRequest request) {
        VisitorParkInfoResponse response = visitorService.getParkInfoByPlate(request.getPlate());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/not-found-info")
    public ResponseEntity<List<VisitorNotFoundResponse>> getNotFoundParksByName(
            @RequestBody VisitorNotFoundRequest request) {
        return ResponseEntity.ok(visitorService.getNotFoundByParkingLotName(request.getParkingLotName()));
    }

    @GetMapping("/park-info/{parkId}")
    public ResponseEntity<VisitorParkInfoResponse> getParkInfoById(
            @PathVariable("parkId") Long parkId) {
        VisitorParkInfoResponse response = visitorService.getParkInfoById(parkId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearby-lots")
    public ResponseEntity<Page<NearbyParkingLotResponse>> nearbyLots(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam(name = "radiusKm", defaultValue = "3") double radiusKm,
            Pageable pageable
    ) {
        return ResponseEntity.ok(visitorService.getNearbyLots(lat, lng, radiusKm, pageable));
    }

    @GetMapping("/not-found-info/{adminId}")
    public ResponseEntity<List<VisitorNotFoundResponse>> notFoundByAdmin(@PathVariable("adminId") Long adminId) {
        return ResponseEntity.ok(visitorService.getNotFoundByAdmin(adminId));
    }
}
