package com.project.articket.staff.controller;

import com.project.articket.staff.dto.StaffExhibitionResponseDTO;
import com.project.articket.staff.service.StaffService;
import com.project.articket.staffRequest.dto.StaffRequestCreateDTO;
import com.project.articket.staffRequest.dto.StaffRequestResponseDTO;
import com.project.articket.staffRequest.service.StaffRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffRequestService staffRequestService;
    private final StaffService staffService;

    @PostMapping("/requests")
    public ResponseEntity<Void> requestStaffAuthority(
            Authentication authentication,
            @RequestBody StaffRequestCreateDTO requestDTO
    ) {

        Long memberId =
                (Long) authentication.getPrincipal();

        staffRequestService.requestStaffAuthority(
                memberId,
                requestDTO
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<StaffRequestResponseDTO>> getMyStaffRequests(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication.getPrincipal();

        List<StaffRequestResponseDTO> responseDTO =
                staffRequestService.getMyStaffRequests(memberId);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/exhibitions")
    public ResponseEntity<List<StaffExhibitionResponseDTO>> getMyExhibitions(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication.getPrincipal();

        List<StaffExhibitionResponseDTO> responseDTO =
                staffService.getMyExhibitions(memberId);

        return ResponseEntity.ok(responseDTO);
    }
}