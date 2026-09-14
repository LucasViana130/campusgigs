package com.campusgigs.api.repository;

import com.campusgigs.api.domain.Hiring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HiringRepository extends JpaRepository<Hiring, Long> {
}
