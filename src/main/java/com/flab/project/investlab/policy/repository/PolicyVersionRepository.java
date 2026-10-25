package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.policy.domain.PolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, Long> {

    @Query("""
            select v from PolicyVersion v
            join fetch v.policy p
            left join fetch v.allocations a
            left join fetch a.instrument
            where p.id = :policyId
              and p.member is null
              and v.versionNo = p.latestVersionNo
            """)
    Optional<PolicyVersion> findLatestPresetVersion(@Param("policyId") Long policyId);

    Optional<PolicyVersion> findByIdAndPolicyMemberIsNull(Long id);

    Optional<PolicyVersion> findByPolicyIdAndVersionNo(Long policyId, int versionNo);

    @Query("""
            select v from PolicyVersion v
            join v.policy p
            where p.id = :policyId
              and p.member.id = :memberId
            order by v.versionNo desc
            """)
    List<PolicyVersion> findMemberVersions(@Param("policyId") Long policyId, @Param("memberId") Long memberId);

    @Query("""
            select v from PolicyVersion v
            join fetch v.policy p
            left join fetch v.allocations a
            left join fetch a.instrument
            where p.id = :policyId
              and v.id = :versionId
              and p.member.id = :memberId
            """)
    Optional<PolicyVersion> findMemberVersion(
            @Param("policyId") Long policyId,
            @Param("versionId") Long versionId,
            @Param("memberId") Long memberId
    );

    @Query("""
            select v from PolicyVersion v
            join fetch v.policy p
            left join fetch v.allocations a
            left join fetch a.instrument
            where p.id = :policyId
              and p.member.id = :memberId
              and v.versionNo = p.latestVersionNo
            """)
    Optional<PolicyVersion> findLatestMemberVersion(@Param("policyId") Long policyId, @Param("memberId") Long memberId);
}
