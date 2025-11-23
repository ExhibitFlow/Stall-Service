package com.exhibitflow.stall.controller;

import com.exhibitflow.stall.dto.CreateStallRequest;
import com.exhibitflow.stall.dto.StallResponse;
import com.exhibitflow.stall.dto.UpdateStallRequest;
import com.exhibitflow.stall.model.StallSize;
import com.exhibitflow.stall.model.StallStatus;
import com.exhibitflow.stall.service.StallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stalls")
@RequiredArgsConstructor
@Tag(name = "Stall Management", description = "APIs for managing exhibition stalls")
@SecurityRequirement(name = "bearer-jwt")
public class StallController {

    private final StallService stallService;

    @GetMapping
    @Operation(summary = "List all stalls with filtering and pagination", 
               description = "Requires: VIEWER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<StallResponse>> getStalls(
            @Parameter(description = "Filter by status") @RequestParam(required = false) StallStatus status,
            @Parameter(description = "Filter by size") @RequestParam(name = "stallSize", required = false) StallSize stallSize,
            @Parameter(description = "Filter by location (partial match)") @RequestParam(required = false) String location,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<StallResponse> stalls = stallService.getStalls(status, stallSize, location, pageable);
        return ResponseEntity.ok(stalls);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a stall by ID",
               description = "Requires: VIEWER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> getStallById(@PathVariable Long id) {
        StallResponse stall = stallService.getStallById(id);
        return ResponseEntity.ok(stall);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get a stall by code",
               description = "Requires: VIEWER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> getStallByCode(@PathVariable String code) {
        StallResponse stall = stallService.getStallByCode(code);
        return ResponseEntity.ok(stall);
    }

    @PostMapping
    @Operation(summary = "Create a new stall",
               description = "Requires: ADMIN role")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StallResponse> createStall(@Valid @RequestBody CreateStallRequest request) {
        StallResponse stall = stallService.createStall(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stall);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a stall",
               description = "Requires: MANAGER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> updateStall(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStallRequest request
    ) {
        StallResponse stall = stallService.updateStall(id, request);
        return ResponseEntity.ok(stall);
    }

    @PostMapping("/{id}/hold")
    @Operation(summary = "Hold a stall (idempotent)",
               description = "Requires: MANAGER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> holdStall(@PathVariable Long id) {
        StallResponse stall = stallService.holdStall(id);
        return ResponseEntity.ok(stall);
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release a stall (idempotent)",
               description = "Requires: MANAGER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> releaseStall(@PathVariable Long id) {
        StallResponse stall = stallService.releaseStall(id);
        return ResponseEntity.ok(stall);
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve a stall (idempotent)",
               description = "Requires: MANAGER role or higher")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<StallResponse> reserveStall(@PathVariable Long id) {
        StallResponse stall = stallService.reserveStall(id);
        return ResponseEntity.ok(stall);
    }
}
