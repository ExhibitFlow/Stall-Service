package com.exhibitflow.stall.dto;

import com.exhibitflow.stall.model.StallSize;
import com.exhibitflow.stall.model.StallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StallResponse {
    private Long id;
    private String code;
    private StallSize size;
    private String location;
    private BigDecimal price;
    private StallStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
