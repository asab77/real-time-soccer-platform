package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
