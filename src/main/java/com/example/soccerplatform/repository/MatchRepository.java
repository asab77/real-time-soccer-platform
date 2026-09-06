package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByExternalId(Long externalId);

    List<Match> findByLeagueIdOrderByStartTimeAsc(Long leagueId);

    List<Match> findByLeagueIdAndStatusOrderByStartTimeAsc(Long leagueId, MatchStatus status);

    List<Match> findByLeagueIdInOrderByStartTimeAsc(Collection<Long> leagueIds);

    List<Match> findByLeagueIdInAndStatusOrderByStartTimeAsc(
            Collection<Long> leagueIds,
            MatchStatus status
    );
}
