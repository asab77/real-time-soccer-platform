package com.example.soccerplatform.controller;

import com.example.soccerplatform.dto.LeagueResponse;
import com.example.soccerplatform.service.LeagueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping
    public List<LeagueResponse> getLeagues() {
        return leagueService.getAvailableLeagues();
    }
}
