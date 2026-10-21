package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.policy.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findAllByMemberIsNullOrderByIdAsc();
}
