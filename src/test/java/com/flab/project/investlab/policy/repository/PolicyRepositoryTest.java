package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.TestcontainersConfiguration;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

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

    private Policy persistPolicy(final Member member, final String name, final int versionCount) {
        final Policy policy = entityManager.persist(new Policy(member, name));
        for (int versionNo = 1; versionNo <= versionCount; versionNo++) {
            entityManager.persist(PolicyVersion.create(policy, null, versionNo, 1, 500, true, 10_000, List.of()));
        }
        return policy;
    }
}
