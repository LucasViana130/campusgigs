package com.campusgigs.api.service;

import com.campusgigs.api.domain.Gig;
import com.campusgigs.api.domain.GigStatus;
import com.campusgigs.api.domain.Hiring;
import com.campusgigs.api.domain.HiringStatus;
import com.campusgigs.api.domain.User;
import com.campusgigs.api.dto.HiringRequest;
import com.campusgigs.api.dto.HiringResponse;
import com.campusgigs.api.exception.BusinessRuleViolationException;
import com.campusgigs.api.repository.HiringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HiringService {

    private final HiringRepository hiringRepository;
    private final GigService gigService;

    @Transactional
    public HiringResponse hire(HiringRequest request, User authenticatedUser) {
        Gig gig = gigService.findOrThrow(request.gigId());

        // Regra: um usuario nao pode contratar o proprio servico.
        if (gig.isOwnedBy(authenticatedUser)) {
            throw new BusinessRuleViolationException("Voce nao pode contratar o proprio servico");
        }

        // Regra: um servico so pode ser contratado se estiver ATIVO.
        if (gig.getStatus() != GigStatus.ATIVO) {
            throw new BusinessRuleViolationException(
                    "Este servico nao esta ativo no momento (situacao atual: " + gig.getStatus() + ")");
        }

        // O contratante e SEMPRE o usuario autenticado (JWT) - nunca um valor vindo do corpo da requisicao.
        Hiring hiring = Hiring.builder()
                .gig(gig)
                .hirer(authenticatedUser)
                .status(HiringStatus.SOLICITADA)
                .build();

        return HiringResponse.from(hiringRepository.save(hiring));
    }
}
