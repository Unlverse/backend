package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.VisitorParkInfoRequest;
import com.tikkeul.mote.dto.VisitorParkInfoResponse;
import com.tikkeul.mote.service.ParkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
