package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.TestcontainersConfiguration;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PolicyRepositoryTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void 회원_규칙을_제외한_프리셋만_ID_순서로_조회한다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy memberPolicy = entityManager.persist(new Policy(member, "내 규칙"));
        final Policy preset = entityManager.persist(new Policy(null, "테스트 프리셋"));
        entityManager.flush();
        entityManager.clear();

        // when
        final List<Policy> presets = policyRepository.findAllByMemberIsNullOrderByIdAsc();

        // then
        assertThat(presets).extracting(Policy::getId)
                .contains(preset.getId())
                .doesNotContain(memberPolicy.getId())
                .isSorted();
    }

    @Test
    void 내_규칙만_최신_버전_번호와_함께_ID_역순으로_조회한다() {
        // given
        final Member me = entityManager.persist(new Member("me@example.com", "encoded-password", "me"));
        final Member other = entityManager.persist(new Member("other@example.com", "encoded-password", "other"));
        final Policy first = persistPolicy(me, "첫 규칙", 2);
        final Policy second = persistPolicy(me, "두번째 규칙", 1);
        persistPolicy(other, "다른 회원 규칙", 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final List<Policy> policies = policyRepository.findAllByMemberIdOrderByIdDesc(me.getId());

        // then
        assertThat(policies)
                .extracting(Policy::getId, Policy::getLatestVersionNo)
                .containsExactly(
                        tuple(second.getId(), 1),
                        tuple(first.getId(), 2)
                );
    }

    @Test
    void 내_규칙을_수정용으로_조회하면_쓰기_잠금을_획득한다() {
        // given
        final Member member = entityManager.persist(new Member("owner@example.com", "encoded-password", "owner"));
        final Policy policy = persistPolicy(member, "내 규칙", 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Policy found = policyRepository.findMemberPolicyForUpdate(policy.getId(), member.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(policy.getId());
        assertThat(entityManager.getEntityManager().getLockMode(found)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void 다른_회원의_규칙은_수정용으로_조회하지_않는다() {
        // given
        final Member owner = entityManager.persist(new Member("owner@example.com", "encoded-password", "owner"));
        final Member other = entityManager.persist(new Member("other@example.com", "encoded-password", "other"));
        final Policy policy = persistPolicy(owner, "주인 규칙", 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<Policy> found = policyRepository.findMemberPolicyForUpdate(policy.getId(), other.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 프리셋은_수정용으로_조회하지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "member"));
        final Policy preset = persistPolicy(null, "프리셋", 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<Policy> found = policyRepository.findMemberPolicyForUpdate(preset.getId(), member.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 없는_규칙은_수정용으로_조회하지_않는다() {
        // when
        final Optional<Policy> found = policyRepository.findMemberPolicyForUpdate(Long.MAX_VALUE, Long.MAX_VALUE);

        // then
        assertThat(found).isEmpty();
    }

    private Policy persistPolicy(final Member member, final String name, final int versionCount) {
        final Policy policy = entityManager.persist(new Policy(member, name));
        for (int versionNo = 1; versionNo <= versionCount; versionNo++) {
            entityManager.persist(PolicyVersion.create(policy, null, versionNo, 1, 500, true, 10_000, null, List.of()));
        }
        return policy;
    }
}
