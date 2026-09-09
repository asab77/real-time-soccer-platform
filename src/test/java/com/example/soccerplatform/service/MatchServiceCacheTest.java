package com.example.soccerplatform.service;

import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class MatchServiceCacheTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private MatchRepository matchRepository;

    @MockitoBean
    private LeagueRepository leagueRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LeaguePreferenceRepository leaguePreferenceRepository;

    @BeforeEach
    void setUp() {
        clearCache("leagueMatches");
        clearCache("userMatches");
        reset(matchRepository, leagueRepository, userRepository, leaguePreferenceRepository);
        when(leagueRepository.existsById(org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
        when(userRepository.existsById(org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);
        when(leaguePreferenceRepository.findByUserIdOrderByLeagueNameAsc(
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(List.of());
    }

    @Test
    void repeatedLeagueAndStatusUsesCachedResponse() {
        when(matchRepository.findByLeagueIdAndStatusOrderByStartTimeAsc(1L, MatchStatus.LIVE))
                .thenReturn(List.of());

        matchService.getLeagueMatches(1L, MatchStatus.LIVE);
        matchService.getLeagueMatches(1L, MatchStatus.LIVE);

        verify(matchRepository, times(1))
                .findByLeagueIdAndStatusOrderByStartTimeAsc(1L, MatchStatus.LIVE);
    }

    @Test
    void differentLeagueIdsUseDifferentKeys() {
        when(matchRepository.findByLeagueIdOrderByStartTimeAsc(1L)).thenReturn(List.of());
        when(matchRepository.findByLeagueIdOrderByStartTimeAsc(2L)).thenReturn(List.of());

        matchService.getLeagueMatches(1L, null);
        matchService.getLeagueMatches(2L, null);

        verify(matchRepository).findByLeagueIdOrderByStartTimeAsc(1L);
        verify(matchRepository).findByLeagueIdOrderByStartTimeAsc(2L);
    }

    @Test
    void differentStatusFiltersUseDifferentKeys() {
        when(matchRepository.findByLeagueIdOrderByStartTimeAsc(1L)).thenReturn(List.of());
        when(matchRepository.findByLeagueIdAndStatusOrderByStartTimeAsc(1L, MatchStatus.LIVE))
                .thenReturn(List.of());

        matchService.getLeagueMatches(1L, null);
        matchService.getLeagueMatches(1L, MatchStatus.LIVE);

        verify(matchRepository).findByLeagueIdOrderByStartTimeAsc(1L);
        verify(matchRepository).findByLeagueIdAndStatusOrderByStartTimeAsc(1L, MatchStatus.LIVE);
    }

    @Test
    void differentUsersUseDifferentKeys() {
        matchService.getUserMatches(5L, MatchStatus.LIVE);
        matchService.getUserMatches(6L, MatchStatus.LIVE);
        matchService.getUserMatches(5L, MatchStatus.LIVE);

        verify(leaguePreferenceRepository).findByUserIdOrderByLeagueNameAsc(5L);
        verify(leaguePreferenceRepository).findByUserIdOrderByLeagueNameAsc(6L);
    }

    private void clearCache(String name) {
        if (cacheManager.getCache(name) != null) {
            cacheManager.getCache(name).clear();
        }
    }
}
