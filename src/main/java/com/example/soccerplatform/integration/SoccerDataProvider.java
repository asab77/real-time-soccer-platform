package com.example.soccerplatform.integration;

import java.time.LocalDate;
import java.util.List;

public interface SoccerDataProvider {

    List<ProviderFixture> getFixtures(String leagueName, LocalDate dateFrom, LocalDate dateTo);
}
