package com.campusgigs.api.service;

import com.campusgigs.api.domain.Role;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.LoginRequest;
import com.campusgigs.api.dto.RegisterRequest;
import com.campusgigs.api.dto.UserResponse;
import com.campusgigs.api.exception.EmailAlreadyInUseException;
import com.campusgigs.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .cep(request.cep())
                .role(Role.USER)
                .build();

        // A resolucao de cidade/UF a partir do CEP (via HttpExchange) e
        // adicionada no Checkpoint 5, junto com a integracao externa.

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public UserResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado nao encontrado apos autenticacao bem-sucedida"));

        return UserResponse.from(user);
    }
}
