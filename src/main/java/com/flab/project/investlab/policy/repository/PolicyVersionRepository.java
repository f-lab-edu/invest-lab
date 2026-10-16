package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.policy.domain.PolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, Long> {

    @Query("""
            select v from PolicyVersion v
            join fetch v.policy p
            left join fetch v.allocations a
            left join fetch a.instrument
            where p.id = :policyId
              and p.member is null
              and v.versionNo = (select max(v2.versionNo) from PolicyVersion v2 where v2.policy = p)
            """)
    Optional<PolicyVersion> findLatestPresetVersion(@Param("policyId") Long policyId);

    Optional<PolicyVersion> findByIdAndPolicyMemberIsNull(Long id);
}
