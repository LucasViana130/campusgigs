package com.campusgigs.api.controller;

import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.UpdateCepRequest;
import com.campusgigs.api.dto.UserResponse;
import com.campusgigs.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint protegido usado (entre outras coisas) para validar manualmente
 * o fluxo de JWT: sem token -> 401; com token valido -> dados do usuario logado.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return UserResponse.from(currentUser);
    }

    /** Atualiza o CEP do usuario autenticado, re-resolvendo cidade/UF via HttpExchange. */
    @PutMapping("/me/cep")
    public UserResponse updateCep(@Valid @RequestBody UpdateCepRequest request,
                                   @AuthenticationPrincipal User currentUser) {
        return userService.updateCep(currentUser, request);
    }
}
