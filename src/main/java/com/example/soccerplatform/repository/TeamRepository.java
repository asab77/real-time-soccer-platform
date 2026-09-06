package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByExternalId(Long externalId);
}
