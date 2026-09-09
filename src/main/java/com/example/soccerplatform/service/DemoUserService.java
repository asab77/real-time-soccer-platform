package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.DemoUserResponse;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.exception.ResourceNotFoundException;
import com.example.soccerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoUserService {
    private final UserRepository users;

    public DemoUserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public DemoUserResponse getDemoUser() {
        User user = users.findAll().stream()
                .filter(candidate -> candidate.getName().equals("Demo User"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Demo user not initialized"));

        return new DemoUserResponse(user.getId(), user.getName());
    }
}
