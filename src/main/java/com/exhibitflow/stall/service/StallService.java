package com.exhibitflow.stall.service;

import com.exhibitflow.stall.dto.*;
import com.exhibitflow.stall.event.StallEventPublisher;
import com.exhibitflow.stall.model.Stall;
import com.exhibitflow.stall.model.StallSize;
import com.exhibitflow.stall.model.StallStatus;
import com.exhibitflow.stall.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StallService {

    private final StallRepository stallRepository;
    private final StallEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<StallResponse> getStalls(StallStatus status, StallSize size, String location, Pageable pageable) {
        log.info("Fetching stalls with filters - status: {}, size: {}, location: {}", status, size, location);
        return stallRepository.findByFilters(status, size, location, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public StallResponse getStallById(Long id) {
        log.info("Fetching stall by id: {}", id);
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with id: " + id));
        return mapToResponse(stall);
    }

    @Transactional(readOnly = true)
    public StallResponse getStallByCode(String code) {
        log.info("Fetching stall by code: {}", code);
        Stall stall = stallRepository.findByCode(code)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with code: " + code));
        return mapToResponse(stall);
    }

    @Transactional
    public StallResponse createStall(CreateStallRequest request) {
        log.info("Creating new stall with code: {}", request.getCode());
        
        // Check if code already exists
        if (stallRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateStallCodeException("Stall with code " + request.getCode() + " already exists");
        }

        Stall stall = Stall.builder()
                .code(request.getCode())
                .size(request.getSize())
                .location(request.getLocation())
                .price(request.getPrice())
                .status(StallStatus.AVAILABLE)
                .build();

        Stall savedStall = stallRepository.save(stall);
        log.info("Created stall with id: {}", savedStall.getId());
        return mapToResponse(savedStall);
    }

    @Transactional
    public StallResponse updateStall(Long id, UpdateStallRequest request) {
        log.info("Updating stall with id: {}", id);
        
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with id: " + id));

        // Check if code is being updated and if it already exists
        if (request.getCode() != null && !request.getCode().equals(stall.getCode())) {
            if (stallRepository.findByCode(request.getCode()).isPresent()) {
                throw new DuplicateStallCodeException("Stall with code " + request.getCode() + " already exists");
            }
            stall.setCode(request.getCode());
        }

        if (request.getSize() != null) {
            stall.setSize(request.getSize());
        }
        if (request.getLocation() != null) {
            stall.setLocation(request.getLocation());
        }
        if (request.getPrice() != null) {
            stall.setPrice(request.getPrice());
        }

        Stall updatedStall = stallRepository.save(stall);
        log.info("Updated stall with id: {}", updatedStall.getId());
        return mapToResponse(updatedStall);
    }

    @Transactional
    public StallResponse holdStall(Long id) {
        log.info("Holding stall with id: {}", id);
        
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with id: " + id));

        // Idempotent: if already held, return current state
        if (stall.getStatus() == StallStatus.HELD) {
            log.info("Stall {} is already held", id);
            return mapToResponse(stall);
        }

        if (stall.getStatus() != StallStatus.AVAILABLE) {
            throw new InvalidStallStatusException(
                    "Cannot hold stall with status: " + stall.getStatus() + ". Only AVAILABLE stalls can be held.");
        }

        stall.setStatus(StallStatus.HELD);
        Stall updatedStall = stallRepository.save(stall);
        log.info("Stall {} held successfully", id);
        return mapToResponse(updatedStall);
    }

    @Transactional
    public StallResponse releaseStall(Long id) {
        log.info("Releasing stall with id: {}", id);
        
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with id: " + id));

        // Idempotent: if already available, return current state
        if (stall.getStatus() == StallStatus.AVAILABLE) {
            log.info("Stall {} is already available", id);
            return mapToResponse(stall);
        }

        if (stall.getStatus() != StallStatus.HELD && stall.getStatus() != StallStatus.RESERVED) {
            throw new InvalidStallStatusException(
                    "Cannot release stall with status: " + stall.getStatus() + ". Only HELD or RESERVED stalls can be released.");
        }

        stall.setStatus(StallStatus.AVAILABLE);
        Stall updatedStall = stallRepository.save(stall);

        // Publish release event
        StallEventDto event = StallEventDto.builder()
                .stallId(updatedStall.getId())
                .code(updatedStall.getCode())
                .status(updatedStall.getStatus())
                .location(updatedStall.getLocation())
                .build();
        eventPublisher.publishStallReleased(event);

        log.info("Stall {} released successfully", id);
        return mapToResponse(updatedStall);
    }

    @Transactional
    public StallResponse reserveStall(Long id) {
        log.info("Reserving stall with id: {}", id);
        
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new StallNotFoundException("Stall not found with id: " + id));

        // Idempotent: if already reserved, return current state
        if (stall.getStatus() == StallStatus.RESERVED) {
            log.info("Stall {} is already reserved", id);
            return mapToResponse(stall);
        }

        if (stall.getStatus() != StallStatus.HELD) {
            throw new InvalidStallStatusException(
                    "Cannot reserve stall with status: " + stall.getStatus() + ". Only HELD stalls can be reserved.");
        }

        stall.setStatus(StallStatus.RESERVED);
        Stall updatedStall = stallRepository.save(stall);

        // Publish reserve event
        StallEventDto event = StallEventDto.builder()
                .stallId(updatedStall.getId())
                .code(updatedStall.getCode())
                .status(updatedStall.getStatus())
                .location(updatedStall.getLocation())
                .build();
        eventPublisher.publishStallReserved(event);

        log.info("Stall {} reserved successfully", id);
        return mapToResponse(updatedStall);
    }

    private StallResponse mapToResponse(Stall stall) {
        return StallResponse.builder()
                .id(stall.getId())
                .code(stall.getCode())
                .size(stall.getSize())
                .location(stall.getLocation())
                .price(stall.getPrice())
                .status(stall.getStatus())
                .createdAt(stall.getCreatedAt())
                .updatedAt(stall.getUpdatedAt())
                .build();
    }
}
