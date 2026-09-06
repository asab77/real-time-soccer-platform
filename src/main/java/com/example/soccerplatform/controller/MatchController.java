package com.example.soccerplatform.controller;

import com.example.soccerplatform.dto.MatchResponse;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/leagues/{leagueId}/matches")
    public List<MatchResponse> getLeagueMatches(
            @PathVariable Long leagueId,
            @RequestParam(required = false) MatchStatus status
    ) {
        return matchService.getLeagueMatches(leagueId, status);
    }

    @GetMapping("/users/{userId}/matches")
    public List<MatchResponse> getUserMatches(
            @PathVariable Long userId,
            @RequestParam(required = false) MatchStatus status
    ) {
        return matchService.getUserMatches(userId, status);
    }
}
