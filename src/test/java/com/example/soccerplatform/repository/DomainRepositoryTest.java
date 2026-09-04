package com.example.soccerplatform.repository;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.LeaguePreference;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DomainRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeaguePreferenceRepository leaguePreferenceRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    void savesLeaguePreferenceAndMatchRelationships() {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Liverpool"));

        LeaguePreference preference = leaguePreferenceRepository.save(
                new LeaguePreference(user, league)
        );
        Match match = matchRepository.save(new Match(
                league,
                homeTeam,
                awayTeam,
                OffsetDateTime.parse("2026-09-12T15:00:00Z"),
                MatchStatus.SCHEDULED
        ));

        assertThat(preference.getId()).isNotNull();
        assertThat(match.getId()).isNotNull();
        assertThat(match.getLeague().getId()).isEqualTo(league.getId());
        assertThat(match.getHomeScore()).isNull();
        assertThat(match.getAwayScore()).isNull();
    }

    @Test
    void rejectsDuplicateLeaguePreference() {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));

        leaguePreferenceRepository.saveAndFlush(new LeaguePreference(user, league));

        assertThatThrownBy(() -> leaguePreferenceRepository.saveAndFlush(
                new LeaguePreference(user, league)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMatchWithSameHomeAndAwayTeam() {
        League league = leagueRepository.save(new League("Premier League"));
        Team team = teamRepository.save(new Team("Arsenal"));

        Match invalidMatch = new Match(
                league,
                team,
                team,
                OffsetDateTime.parse("2026-09-12T15:00:00Z"),
                MatchStatus.SCHEDULED
        );

        assertThatThrownBy(() -> matchRepository.saveAndFlush(invalidMatch))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
