package com.flab.project.investlab.auth.repository;

import com.flab.project.investlab.auth.domain.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession> findByMemberId(Long memberId);

    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
