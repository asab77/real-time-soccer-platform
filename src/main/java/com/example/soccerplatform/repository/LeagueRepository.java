package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Long> {
}
