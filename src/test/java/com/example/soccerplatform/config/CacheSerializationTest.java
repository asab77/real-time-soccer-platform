package com.example.soccerplatform.config;

import com.example.soccerplatform.dto.MatchResponse;
import com.example.soccerplatform.entity.MatchStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CacheSerializationTest {

    @Autowired
    private RedisCacheConfiguration redisCacheConfiguration;

    @Test
    void roundTripsMatchResponseListAsJson() {
        MatchResponse response = new MatchResponse(
                1L,
                39L,
                "Premier League",
                42L,
                "Arsenal",
                49L,
                "Chelsea",
                OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                1,
                0,
                MatchStatus.LIVE
        );
        List<MatchResponse> original = new ArrayList<>(List.of(response));
        RedisSerializationContext.SerializationPair<Object> serializer =
                redisCacheConfiguration.getValueSerializationPair();

        ByteBuffer json = serializer.write(original);
        Object restored = serializer.read(json);

        assertThat(restored).isEqualTo(original);
    }
}
