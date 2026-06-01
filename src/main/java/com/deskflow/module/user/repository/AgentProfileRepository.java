package com.deskflow.module.user.repository;

import com.deskflow.module.user.domain.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentProfileRepository extends JpaRepository<AgentProfile, UUID> {

    Optional<AgentProfile> findByUserId(UUID userId);

    List<AgentProfile> findByAvailableTrueOrderByCurrentLoadAsc();

    @Query("SELECT ap FROM AgentProfile ap JOIN ap.expertise e WHERE ap.available = true AND e = :category ORDER BY ap.currentLoad ASC")
    List<AgentProfile> findAvailableByExpertise(@Param("category") String category);
}