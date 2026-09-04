package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
