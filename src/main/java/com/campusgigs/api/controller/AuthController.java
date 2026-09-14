package com.campusgigs.api.controller;

import com.campusgigs.api.dto.LoginRequest;
import com.campusgigs.api.dto.RegisterRequest;
import com.campusgigs.api.dto.UserResponse;
import com.campusgigs.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        // A partir do Checkpoint 3 este endpoint passa a retornar tambem o
        // token JWT; por enquanto apenas confirma que as credenciais sao validas.
        return ResponseEntity.ok(authService.login(request));
    }
}
