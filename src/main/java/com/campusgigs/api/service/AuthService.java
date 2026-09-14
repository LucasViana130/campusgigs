package com.campusgigs.api.service;

import com.campusgigs.api.client.CepLookupResult;
import com.campusgigs.api.client.CepLookupService;
import com.campusgigs.api.domain.Role;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.AuthResponse;
import com.campusgigs.api.dto.LoginRequest;
import com.campusgigs.api.dto.RegisterRequest;
import com.campusgigs.api.dto.UserResponse;
import com.campusgigs.api.exception.EmailAlreadyInUseException;
import com.campusgigs.api.repository.UserRepository;
import com.campusgigs.api.security.JwtService;
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
    private final JwtService jwtService;
    private final CepLookupService cepLookupService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        // CEP e resolvido em cidade/UF via cliente HttpExchange ANTES de
        // persistir o usuario: um CEP invalido/inexistente interrompe o
        // cadastro com um erro claro, em vez de deixar city/state incompletos.
        CepLookupResult location = cepLookupService.lookup(request.cep());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .cep(request.cep())
                .city(location.city())
                .state(location.state())
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario autenticado nao encontrado apos autenticacao bem-sucedida"));

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(user));
    }
}
