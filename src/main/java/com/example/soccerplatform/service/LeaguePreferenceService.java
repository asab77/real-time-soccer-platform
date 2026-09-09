package com.example.soccerplatform.service;

import com.example.soccerplatform.cache.MatchCacheInvalidationEvent;
import com.example.soccerplatform.dto.CreateLeaguePreferenceRequest;
import com.example.soccerplatform.dto.LeaguePreferenceResponse;
import com.example.soccerplatform.dto.LeagueResponse;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.LeaguePreference;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.exception.DuplicateLeaguePreferenceException;
import com.example.soccerplatform.exception.ResourceNotFoundException;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeaguePreferenceService {

    private final UserRepository userRepository;
    private final LeagueRepository leagueRepository;
    private final LeaguePreferenceRepository leaguePreferenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LeaguePreferenceService(
            UserRepository userRepository,
            LeagueRepository leagueRepository,
            LeaguePreferenceRepository leaguePreferenceRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.leagueRepository = leagueRepository;
        this.leaguePreferenceRepository = leaguePreferenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LeaguePreferenceResponse createPreference(
            Long userId,
            CreateLeaguePreferenceRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        League league = leagueRepository.findById(request.leagueId())
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        if (leaguePreferenceRepository.existsByUserIdAndLeagueId(userId, request.leagueId())) {
            throw duplicatePreference();
        }

        LeaguePreference preference = new LeaguePreference(user, league);

        try {
            leaguePreferenceRepository.saveAndFlush(preference);
        } catch (DataIntegrityViolationException exception) {
            throw duplicatePreference();
        }

        eventPublisher.publishEvent(MatchCacheInvalidationEvent.userMatchCache());
        return new LeaguePreferenceResponse(userId, request.leagueId());
    }

    @Transactional(readOnly = true)
    public List<LeagueResponse> getPreferences(Long userId) {
        verifyUserExists(userId);

        return leaguePreferenceRepository.findByUserIdOrderByLeagueNameAsc(userId)
                .stream()
                .map(preference -> new LeagueResponse(
                        preference.getLeague().getId(),
                        preference.getLeague().getName()
                ))
                .toList();
    }

    @Transactional
    public void deletePreference(Long userId, Long leagueId) {
        verifyUserExists(userId);

        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found");
        }

        LeaguePreference preference = leaguePreferenceRepository
                .findByUserIdAndLeagueId(userId, leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League preference not found"));

        leaguePreferenceRepository.delete(preference);
        eventPublisher.publishEvent(MatchCacheInvalidationEvent.userMatchCache());
    }

    private void verifyUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private DuplicateLeaguePreferenceException duplicatePreference() {
        return new DuplicateLeaguePreferenceException("User already follows this league");
    }
}
