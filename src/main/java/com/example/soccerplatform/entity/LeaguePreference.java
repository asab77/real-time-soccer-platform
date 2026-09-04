package com.example.soccerplatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "league_preference",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_league_preference_user_league",
                columnNames = {"user_id", "league_id"}
        )
)
public class LeaguePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    protected LeaguePreference() {
    }

    public LeaguePreference(User user, League league) {
        this.user = user;
        this.league = league;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public League getLeague() {
        return league;
    }
}
