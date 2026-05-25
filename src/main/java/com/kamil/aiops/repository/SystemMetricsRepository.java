package com.kamil.aiops.repository;

import com.kamil.aiops.entity.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SystemMetrics entity
 */
@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {

}

