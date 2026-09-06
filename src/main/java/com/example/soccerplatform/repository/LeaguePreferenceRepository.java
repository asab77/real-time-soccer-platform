package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.LeaguePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaguePreferenceRepository extends JpaRepository<LeaguePreference, Long> {

    boolean existsByUserIdAndLeagueId(Long userId, Long leagueId);

    List<LeaguePreference> findByUserIdOrderByLeagueNameAsc(Long userId);

    Optional<LeaguePreference> findByUserIdAndLeagueId(Long userId, Long leagueId);
}
