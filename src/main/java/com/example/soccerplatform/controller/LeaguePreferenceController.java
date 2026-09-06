package com.example.soccerplatform.controller;

import com.example.soccerplatform.dto.CreateLeaguePreferenceRequest;
import com.example.soccerplatform.dto.LeaguePreferenceResponse;
import com.example.soccerplatform.dto.LeagueResponse;
import com.example.soccerplatform.service.LeaguePreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/preferences")
public class LeaguePreferenceController {

    private final LeaguePreferenceService leaguePreferenceService;

    public LeaguePreferenceController(LeaguePreferenceService leaguePreferenceService) {
        this.leaguePreferenceService = leaguePreferenceService;
    }

    @PostMapping
    public ResponseEntity<LeaguePreferenceResponse> createPreference(
            @PathVariable Long userId,
            @Valid @RequestBody CreateLeaguePreferenceRequest request
    ) {
        LeaguePreferenceResponse response = leaguePreferenceService.createPreference(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<LeagueResponse> getPreferences(@PathVariable Long userId) {
        return leaguePreferenceService.getPreferences(userId);
    }

    @DeleteMapping("/{leagueId}")
    public ResponseEntity<Void> deletePreference(
            @PathVariable Long userId,
            @PathVariable Long leagueId
    ) {
        leaguePreferenceService.deletePreference(userId, leagueId);
        return ResponseEntity.noContent().build();
    }
}
