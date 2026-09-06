package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.MatchResponse;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.exception.ResourceNotFoundException;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final LeaguePreferenceRepository leaguePreferenceRepository;

    public MatchService(
            MatchRepository matchRepository,
            LeagueRepository leagueRepository,
            UserRepository userRepository,
            LeaguePreferenceRepository leaguePreferenceRepository
    ) {
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.leaguePreferenceRepository = leaguePreferenceRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getLeagueMatches(Long leagueId, MatchStatus status) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found");
        }

        List<Match> matches = status == null
                ? matchRepository.findByLeagueIdOrderByStartTimeAsc(leagueId)
                : matchRepository.findByLeagueIdAndStatusOrderByStartTimeAsc(leagueId, status);

        return toResponses(matches);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getUserMatches(Long userId, MatchStatus status) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        List<Long> leagueIds = leaguePreferenceRepository
                .findByUserIdOrderByLeagueNameAsc(userId)
                .stream()
                .map(preference -> preference.getLeague().getId())
                .toList();

        if (leagueIds.isEmpty()) {
            return List.of();
        }

        List<Match> matches = status == null
                ? matchRepository.findByLeagueIdInOrderByStartTimeAsc(leagueIds)
                : matchRepository.findByLeagueIdInAndStatusOrderByStartTimeAsc(leagueIds, status);

        return toResponses(matches);
    }

    private List<MatchResponse> toResponses(List<Match> matches) {
        return matches.stream()
                .map(this::toResponse)
                .toList();
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getLeague().getId(),
                match.getLeague().getName(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getStartTime(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getStatus()
        );
    }
}
