package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {

    Optional<League> findByExternalId(Long externalId);

    Optional<League> findByNameIgnoreCase(String name);
}
