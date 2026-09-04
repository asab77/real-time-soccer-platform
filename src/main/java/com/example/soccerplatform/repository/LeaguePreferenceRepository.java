package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.LeaguePreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaguePreferenceRepository extends JpaRepository<LeaguePreference, Long> {
}
