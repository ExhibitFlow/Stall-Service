package com.exhibitflow.stall.service;

import com.exhibitflow.stall.dto.CreateStallRequest;
import com.exhibitflow.stall.dto.StallResponse;
import com.exhibitflow.stall.dto.UpdateStallRequest;
import com.exhibitflow.stall.event.StallEventPublisher;
import com.exhibitflow.stall.model.Stall;
import com.exhibitflow.stall.model.StallSize;
import com.exhibitflow.stall.model.StallStatus;
import com.exhibitflow.stall.repository.StallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StallServiceTest {

    @Mock
    private StallRepository stallRepository;

    @Mock
    private StallEventPublisher eventPublisher;

    @InjectMocks
    private StallService stallService;

    private Stall testStall;

    @BeforeEach
    void setUp() {
        testStall = Stall.builder()
                .id(1L)
                .code("A-001")
                .size(StallSize.MEDIUM)
                .location("Hall A")
                .price(new BigDecimal("500.00"))
                .status(StallStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getStalls_shouldReturnPagedStalls() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Stall> stallPage = new PageImpl<>(List.of(testStall));
        when(stallRepository.findByFilters(null, null, null, pageable)).thenReturn(stallPage);

        // When
        Page<StallResponse> result = stallService.getStalls(null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("A-001");
        verify(stallRepository).findByFilters(null, null, null, pageable);
    }

    @Test
    void getStallById_shouldReturnStall_whenExists() {
        // Given
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When
        StallResponse result = stallService.getStallById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("A-001");
        verify(stallRepository).findById(1L);
    }

    @Test
    void getStallById_shouldThrowException_whenNotFound() {
        // Given
        when(stallRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> stallService.getStallById(1L))
                .isInstanceOf(StallNotFoundException.class)
                .hasMessageContaining("Stall not found with id: 1");
    }

    @Test
    void createStall_shouldCreateAndReturnStall() {
        // Given
        CreateStallRequest request = CreateStallRequest.builder()
                .code("B-002")
                .size(StallSize.LARGE)
                .location("Hall B")
                .price(new BigDecimal("750.00"))
                .build();

        Stall newStall = Stall.builder()
                .id(2L)
                .code("B-002")
                .size(StallSize.LARGE)
                .location("Hall B")
                .price(new BigDecimal("750.00"))
                .status(StallStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stallRepository.findByCode("B-002")).thenReturn(Optional.empty());
        when(stallRepository.save(any(Stall.class))).thenReturn(newStall);

        // When
        StallResponse result = stallService.createStall(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("B-002");
        assertThat(result.getStatus()).isEqualTo(StallStatus.AVAILABLE);
        verify(stallRepository).save(any(Stall.class));
    }

    @Test
    void createStall_shouldThrowException_whenCodeExists() {
        // Given
        CreateStallRequest request = CreateStallRequest.builder()
                .code("A-001")
                .size(StallSize.LARGE)
                .location("Hall B")
                .price(new BigDecimal("750.00"))
                .build();

        when(stallRepository.findByCode("A-001")).thenReturn(Optional.of(testStall));

        // When/Then
        assertThatThrownBy(() -> stallService.createStall(request))
                .isInstanceOf(DuplicateStallCodeException.class)
                .hasMessageContaining("Stall with code A-001 already exists");
    }

    @Test
    void updateStall_shouldUpdateAndReturnStall() {
        // Given
        UpdateStallRequest request = UpdateStallRequest.builder()
                .location("Hall C")
                .price(new BigDecimal("600.00"))
                .build();

        Stall updatedStall = Stall.builder()
                .id(1L)
                .code("A-001")
                .size(StallSize.MEDIUM)
                .location("Hall C")
                .price(new BigDecimal("600.00"))
                .status(StallStatus.AVAILABLE)
                .createdAt(testStall.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));
        when(stallRepository.save(any(Stall.class))).thenReturn(updatedStall);

        // When
        StallResponse result = stallService.updateStall(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocation()).isEqualTo("Hall C");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("600.00"));
        verify(stallRepository).save(any(Stall.class));
    }

    @Test
    void holdStall_shouldChangeStatusToHeld() {
        // Given
        Stall heldStall = Stall.builder()
                .id(1L)
                .code("A-001")
                .size(StallSize.MEDIUM)
                .location("Hall A")
                .price(new BigDecimal("500.00"))
                .status(StallStatus.HELD)
                .createdAt(testStall.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));
        when(stallRepository.save(any(Stall.class))).thenReturn(heldStall);

        // When
        StallResponse result = stallService.holdStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.HELD);
        verify(stallRepository).save(any(Stall.class));
    }

    @Test
    void holdStall_shouldBeIdempotent_whenAlreadyHeld() {
        // Given
        testStall.setStatus(StallStatus.HELD);
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When
        StallResponse result = stallService.holdStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.HELD);
        verify(stallRepository, never()).save(any(Stall.class));
    }

    @Test
    void holdStall_shouldThrowException_whenNotAvailable() {
        // Given
        testStall.setStatus(StallStatus.RESERVED);
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When/Then
        assertThatThrownBy(() -> stallService.holdStall(1L))
                .isInstanceOf(InvalidStallStatusException.class)
                .hasMessageContaining("Cannot hold stall with status: RESERVED");
    }

    @Test
    void releaseStall_shouldChangeStatusToAvailable() {
        // Given
        testStall.setStatus(StallStatus.HELD);
        Stall releasedStall = Stall.builder()
                .id(1L)
                .code("A-001")
                .size(StallSize.MEDIUM)
                .location("Hall A")
                .price(new BigDecimal("500.00"))
                .status(StallStatus.AVAILABLE)
                .createdAt(testStall.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));
        when(stallRepository.save(any(Stall.class))).thenReturn(releasedStall);

        // When
        StallResponse result = stallService.releaseStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.AVAILABLE);
        verify(stallRepository).save(any(Stall.class));
        verify(eventPublisher).publishStallReleased(any());
    }

    @Test
    void releaseStall_shouldBeIdempotent_whenAlreadyAvailable() {
        // Given
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When
        StallResponse result = stallService.releaseStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.AVAILABLE);
        verify(stallRepository, never()).save(any(Stall.class));
        verify(eventPublisher, never()).publishStallReleased(any());
    }

    @Test
    void reserveStall_shouldChangeStatusToReserved() {
        // Given
        testStall.setStatus(StallStatus.HELD);
        Stall reservedStall = Stall.builder()
                .id(1L)
                .code("A-001")
                .size(StallSize.MEDIUM)
                .location("Hall A")
                .price(new BigDecimal("500.00"))
                .status(StallStatus.RESERVED)
                .createdAt(testStall.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));
        when(stallRepository.save(any(Stall.class))).thenReturn(reservedStall);

        // When
        StallResponse result = stallService.reserveStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.RESERVED);
        verify(stallRepository).save(any(Stall.class));
        verify(eventPublisher).publishStallReserved(any());
    }

    @Test
    void reserveStall_shouldBeIdempotent_whenAlreadyReserved() {
        // Given
        testStall.setStatus(StallStatus.RESERVED);
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When
        StallResponse result = stallService.reserveStall(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StallStatus.RESERVED);
        verify(stallRepository, never()).save(any(Stall.class));
        verify(eventPublisher, never()).publishStallReserved(any());
    }

    @Test
    void reserveStall_shouldThrowException_whenNotHeld() {
        // Given
        when(stallRepository.findById(1L)).thenReturn(Optional.of(testStall));

        // When/Then
        assertThatThrownBy(() -> stallService.reserveStall(1L))
                .isInstanceOf(InvalidStallStatusException.class)
                .hasMessageContaining("Cannot reserve stall with status: AVAILABLE");
    }
}
