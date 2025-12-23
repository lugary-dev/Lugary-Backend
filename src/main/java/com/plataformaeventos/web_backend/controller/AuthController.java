package com.plataformaeventos.web_backend.controller;

import com.plataformaeventos.web_backend.dto.LoginRequest;
import com.plataformaeventos.web_backend.dto.LoginResponse;
import com.plataformaeventos.web_backend.service.AuthService;
import jakarta.validation.Valid; // IMPORTANTE
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { // @Valid agregado
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
