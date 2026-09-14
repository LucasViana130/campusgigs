package com.campusgigs.api.repository;

import com.campusgigs.api.domain.Gig;
import com.campusgigs.api.domain.GigStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GigRepository extends JpaRepository<Gig, Long> {

    List<Gig> findAllByStatus(GigStatus status);
}
