package com.flab.project.investlab.policy.repository;

import com.flab.project.investlab.TestcontainersConfiguration;
import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.policy.domain.AllocationDraft;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyAllocation;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PolicyVersionRepositoryTest {

    @Autowired
    private PolicyVersionRepository policyVersionRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void 버전이_여러_개면_가장_큰_버전을_조회한다() {
        // given
        final Policy preset = entityManager.persist(new Policy(null, "테스트 프리셋"));
        persistVersion(preset, 1);
        final PolicyVersion latest = persistVersion(preset, 2);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findLatestPresetVersion(preset.getId());

        // then
        assertThat(found).get()
                .extracting(PolicyVersion::getId, PolicyVersion::getVersionNo)
                .containsExactly(latest.getId(), 2);
    }

    @Test
    void 최신_버전을_조회하면_배분과_ETF를_함께_가져온다() {
        // given
        final Policy preset = entityManager.persist(new Policy(null, "테스트 프리셋"));
        persistVersion(preset, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final PolicyVersion found = policyVersionRepository.findLatestPresetVersion(preset.getId()).orElseThrow();

        // then
        final PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(found, "allocations")).isTrue();
        assertThat(found.getAllocations()).hasSize(1);
        final PolicyAllocation allocation = found.getAllocations().get(0);
        assertThat(persistenceUnitUtil.isLoaded(allocation, "instrument")).isTrue();
    }

    @Test
    void 회원이_만든_규칙은_프리셋으로_조회하지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findLatestPresetVersion(policy.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 존재하지_않는_ID면_빈_결과를_반환한다() {
        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findLatestPresetVersion(Long.MAX_VALUE);

        // then
        assertThat(found).isEmpty();
    }

    private PolicyVersion persistVersion(final Policy policy, final int versionNo) {
        final Instrument instrument = instrumentRepository.findAllByOrderByNameAsc().get(0);
        final PolicyVersion version = PolicyVersion.create(policy, null, versionNo, 1, 500, true, 0, List.of(
                new AllocationDraft("국내주식", instrument, 10_000)
        ));
        return entityManager.persist(version);
    }
}
