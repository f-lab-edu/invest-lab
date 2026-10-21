package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.TestcontainersConfiguration;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.policy.domain.Policy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
