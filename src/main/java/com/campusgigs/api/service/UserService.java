package com.campusgigs.api.service;

import com.campusgigs.api.client.CepLookupResult;
import com.campusgigs.api.client.CepLookupService;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.UpdateCepRequest;
import com.campusgigs.api.dto.UserResponse;
import com.campusgigs.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CepLookupService cepLookupService;

    @Transactional
    public UserResponse updateCep(User authenticatedUser, UpdateCepRequest request) {
        // Mesma regra do cadastro: o CEP so e gravado se a consulta externa
        // for bem-sucedida, garantindo que cidade/UF nunca fiquem inconsistentes.
        CepLookupResult location = cepLookupService.lookup(request.cep());

        authenticatedUser.setCep(request.cep());
        authenticatedUser.setCity(location.city());
        authenticatedUser.setState(location.state());

        User saved = userRepository.save(authenticatedUser);
        return UserResponse.from(saved);
    }
}
