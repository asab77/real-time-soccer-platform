package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.integration.apifootball.ApiFootballFixture;
import com.example.soccerplatform.integration.apifootball.ApiFootballStatusMapper;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FixturePersistenceService {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final ApiFootballStatusMapper statusMapper;

    public FixturePersistenceService(
            LeagueRepository leagueRepository,
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            ApiFootballStatusMapper statusMapper
    ) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.statusMapper = statusMapper;
    }

    @Transactional
    public FixtureSyncSummary persist(List<ApiFootballFixture> fixtures) {
        int created = 0;
        int updated = 0;

        for (ApiFootballFixture fixture : fixtures) {
            validateFixture(fixture);

            League league = findOrCreateLeague(fixture.league());
            Team homeTeam = findOrCreateTeam(fixture.teams().home());
            Team awayTeam = findOrCreateTeam(fixture.teams().away());
            MatchStatus status = statusMapper.toMatchStatus(fixture.fixture().status().shortCode());
            Optional<Match> existingMatch = matchRepository.findByExternalId(fixture.fixture().id());

            Match match;
            if (existingMatch.isPresent()) {
                match = existingMatch.get();
                match.setStartTime(fixture.fixture().date());
                match.setScore(fixture.goals().home(), fixture.goals().away());
                match.setStatus(status);
                updated++;
            } else {
                match = new Match(
                        league,
                        homeTeam,
                        awayTeam,
                        fixture.fixture().date(),
                        status
                );
                match.setExternalId(fixture.fixture().id());
                match.setScore(fixture.goals().home(), fixture.goals().away());
                created++;
            }

            matchRepository.save(match);
        }

        return new FixtureSyncSummary(fixtures.size(), created, updated);
    }

    private League findOrCreateLeague(ApiFootballFixture.League providerLeague) {
        return leagueRepository.findByExternalId(providerLeague.id())
                .map(league -> {
                    league.setName(providerLeague.name());
                    return league;
                })
                .orElseGet(() -> {
                    League league = new League(providerLeague.name());
                    league.setExternalId(providerLeague.id());
                    return leagueRepository.save(league);
                });
    }

    private Team findOrCreateTeam(ApiFootballFixture.Team providerTeam) {
        return teamRepository.findByExternalId(providerTeam.id())
                .map(team -> {
                    team.setName(providerTeam.name());
                    return team;
                })
                .orElseGet(() -> {
                    Team team = new Team(providerTeam.name());
                    team.setExternalId(providerTeam.id());
                    return teamRepository.save(team);
                });
    }

    private void validateFixture(ApiFootballFixture fixture) {
        boolean invalid = fixture == null
                || fixture.fixture() == null
                || fixture.fixture().id() == null
                || fixture.fixture().date() == null
                || fixture.fixture().status() == null
                || fixture.league() == null
                || fixture.league().id() == null
                || fixture.league().name() == null
                || fixture.teams() == null
                || invalidTeam(fixture.teams().home())
                || invalidTeam(fixture.teams().away())
                || fixture.goals() == null;

        if (invalid) {
            throw new ProviderIntegrationException("API-Football returned malformed fixture data");
        }
    }

    private boolean invalidTeam(ApiFootballFixture.Team team) {
        return team == null || team.id() == null || team.name() == null;
    }
}
