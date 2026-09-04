package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.LeagueResponse;
import com.example.soccerplatform.repository.LeagueRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    public List<LeagueResponse> getAvailableLeagues() {
        return leagueRepository.findAll(Sort.by("name"))
                .stream()
                .map(league -> new LeagueResponse(league.getId(), league.getName()))
                .toList();
    }
}
