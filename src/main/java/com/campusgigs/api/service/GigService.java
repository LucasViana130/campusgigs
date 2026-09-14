package com.campusgigs.api.service;

import com.campusgigs.api.domain.Gig;
import com.campusgigs.api.domain.GigStatus;
import com.campusgigs.api.domain.Role;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.GigRequest;
import com.campusgigs.api.dto.GigResponse;
import com.campusgigs.api.exception.ResourceNotFoundException;
import com.campusgigs.api.repository.GigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GigService {

    private final GigRepository gigRepository;

    @Transactional
    public GigResponse create(GigRequest request, User authenticatedUser) {
        // O prestador e SEMPRE o usuario autenticado (JWT) - nunca um valor vindo do corpo da requisicao.
        Gig gig = Gig.builder()
                .provider(authenticatedUser)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .status(GigStatus.ATIVO)
                .build();

        return GigResponse.from(gigRepository.save(gig));
    }

    @Transactional(readOnly = true)
    public List<GigResponse> list(GigStatus statusFilter) {
        List<Gig> gigs = statusFilter == null
                ? gigRepository.findAll()
                : gigRepository.findAllByStatus(statusFilter);

        return gigs.stream().map(GigResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GigResponse getById(Long id) {
        return GigResponse.from(findOrThrow(id));
    }

    @Transactional
    public GigResponse update(Long id, GigRequest request, User authenticatedUser) {
        Gig gig = findOrThrow(id);

        // Regra: um USER so pode editar os proprios servicos. Diferente do
        // encerramento, o enunciado nao concede excecao para ADMIN aqui.
        if (!gig.isOwnedBy(authenticatedUser)) {
            throw new AccessDeniedException("Voce so pode editar servicos que voce mesmo publicou");
        }

        gig.setTitle(request.title());
        gig.setDescription(request.description());
        gig.setCategory(request.category());
        gig.setPrice(request.price());

        return GigResponse.from(gig);
    }

    @Transactional
    public GigResponse close(Long id, User authenticatedUser) {
        Gig gig = findOrThrow(id);

        boolean isOwner = gig.isOwnedBy(authenticatedUser);
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;

        // Regra: USER encerra apenas os proprios servicos; ADMIN encerra qualquer um.
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Voce nao tem permissao para encerrar este servico");
        }

        gig.setStatus(GigStatus.ENCERRADO);
        return GigResponse.from(gig);
    }

    Gig findOrThrow(Long id) {
        return gigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado: id=" + id));
    }
}
