package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.dto.PolicySummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findAllByMemberIsNullOrderByIdAsc();

    @Query("""
            select new com.flab.project.investlab.policy.dto.PolicySummaryResponse(
                p.id, p.name, max(v.versionNo), p.createdAt)
            from PolicyVersion v
            join v.policy p
            where p.member.id = :memberId
            group by p.id, p.name, p.createdAt
            order by p.id desc
            """)
    List<PolicySummaryResponse> findSummariesByMemberId(@Param("memberId") Long memberId);
}
