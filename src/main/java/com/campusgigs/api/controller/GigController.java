package com.campusgigs.api.controller;

import com.campusgigs.api.domain.GigStatus;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.GigRequest;
import com.campusgigs.api.dto.GigResponse;
import com.campusgigs.api.service.GigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
public class GigController {

    private final GigService gigService;

    /** Publicar um servico. Qualquer usuario autenticado pode publicar. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GigResponse create(@Valid @RequestBody GigRequest request,
                               @AuthenticationPrincipal User authenticatedUser) {
        return gigService.create(request, authenticatedUser);
    }

    /** Listar servicos publicados. Endpoint publico (nao exige autenticacao). */
    @GetMapping
    public List<GigResponse> list(@RequestParam(required = false) GigStatus status) {
        return gigService.list(status);
    }

    /** Detalhe de um servico. Endpoint publico. */
    @GetMapping("/{id}")
    public GigResponse getById(@PathVariable Long id) {
        return gigService.getById(id);
    }

    /** Editar um servico proprio. USER so edita os proprios servicos. */
    @PutMapping("/{id}")
    public GigResponse update(@PathVariable Long id,
                               @Valid @RequestBody GigRequest request,
                               @AuthenticationPrincipal User authenticatedUser) {
        return gigService.update(id, request, authenticatedUser);
    }

    /** Encerrar um servico. Dono encerra o proprio; ADMIN encerra qualquer um. */
    @PatchMapping("/{id}/close")
    public GigResponse close(@PathVariable Long id, @AuthenticationPrincipal User authenticatedUser) {
        return gigService.close(id, authenticatedUser);
    }
}
