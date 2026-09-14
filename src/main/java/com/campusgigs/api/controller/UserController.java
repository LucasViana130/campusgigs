package com.campusgigs.api.controller;

import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint protegido usado (entre outras coisas) para validar manualmente
 * o fluxo de JWT: sem token -> 401; com token valido -> dados do usuario logado.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return UserResponse.from(currentUser);
    }
}
