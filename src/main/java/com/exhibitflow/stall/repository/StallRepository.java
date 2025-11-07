package com.exhibitflow.stall.repository;

import com.exhibitflow.stall.model.Stall;
import com.exhibitflow.stall.model.StallSize;
import com.exhibitflow.stall.model.StallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StallRepository extends JpaRepository<Stall, Long> {

    Optional<Stall> findByCode(String code);

    @Query("SELECT s FROM Stall s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:size IS NULL OR s.size = :size) AND " +
           "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Stall> findByFilters(
            @Param("status") StallStatus status,
            @Param("size") StallSize size,
            @Param("location") String location,
            Pageable pageable
    );
}
