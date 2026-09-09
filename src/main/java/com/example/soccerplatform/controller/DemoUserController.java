package com.example.soccerplatform.controller;

import com.example.soccerplatform.dto.DemoUserResponse;
import com.example.soccerplatform.service.DemoUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoUserController {
    private final DemoUserService service;

    public DemoUserController(DemoUserService service) {
        this.service = service;
    }

    @GetMapping("/demo/user")
    public DemoUserResponse getDemoUser() {
        return service.getDemoUser();
    }
}
