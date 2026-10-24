package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.policy.domain.Policy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findAllByMemberIsNullOrderByIdAsc();

    List<Policy> findAllByMemberIdOrderByIdDesc(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Policy p
            where p.id = :policyId
              and p.member.id = :memberId
            """)
    Optional<Policy> findMemberPolicyForUpdate(@Param("policyId") Long policyId, @Param("memberId") Long memberId);
}
