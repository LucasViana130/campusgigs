package com.campusgigs.api.controller;

import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.HiringRequest;
import com.campusgigs.api.dto.HiringResponse;
import com.campusgigs.api.service.HiringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hirings")
@RequiredArgsConstructor
public class HiringController {

    private final HiringService hiringService;

    /** Contratar um servico. Qualquer usuario autenticado pode contratar (exceto o proprio servico). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HiringResponse hire(@Valid @RequestBody HiringRequest request,
                                @AuthenticationPrincipal User authenticatedUser) {
        return hiringService.hire(request, authenticatedUser);
    }
}
