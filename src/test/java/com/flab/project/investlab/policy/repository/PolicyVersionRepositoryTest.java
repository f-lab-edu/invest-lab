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
import static org.assertj.core.api.Assertions.tuple;

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

    @Test
    void 내_규칙을_조회하면_최신_버전을_배분과_함께_가져온다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        persistVersion(policy, 1);
        final PolicyVersion latest = persistVersion(policy, 2);
        entityManager.flush();
        entityManager.clear();

        // when
        final PolicyVersion found = policyVersionRepository.findLatestMemberVersion(policy.getId(), member.getId())
                .orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(latest.getId());
        final PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(found, "allocations")).isTrue();
    }

    @Test
    void 다른_회원의_규칙은_내_규칙으로_조회하지_않는다() {
        // given
        final Member owner = entityManager.persist(new Member("owner@example.com", "encoded-password", "owner"));
        final Member other = entityManager.persist(new Member("other@example.com", "encoded-password", "other"));
        final Policy policy = entityManager.persist(new Policy(owner, "주인 규칙"));
        persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findLatestMemberVersion(policy.getId(), other.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 정책과_번호로_버전을_조회할_때_배분과_정책을_함께_가져오지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final Policy otherPolicy = entityManager.persist(new Policy(member, "다른 규칙"));
        persistVersion(policy, 1);
        final PolicyVersion baseVersion = persistVersion(policy, 2);
        persistVersion(otherPolicy, 2);
        persistVersion(otherPolicy, 3);
        entityManager.flush();
        entityManager.clear();

        // when
        final PolicyVersion found = policyVersionRepository.findByPolicyIdAndVersionNo(policy.getId(), 2)
                .orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(baseVersion.getId());
        final PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(found, "allocations")).isFalse();
        assertThat(persistenceUnitUtil.isLoaded(found, "policy")).isFalse();
    }

    @Test
    void 다른_정책에만_있는_버전_번호면_빈_결과를_반환한다() {
        // given
        final Policy policy = entityManager.persist(new Policy(null, "프리셋"));
        final Policy otherPolicy = entityManager.persist(new Policy(null, "다른 프리셋"));
        persistVersion(policy, 1);
        persistVersion(otherPolicy, 2);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findByPolicyIdAndVersionNo(policy.getId(), 2);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 없는_정책의_버전을_조회하면_빈_결과를_반환한다() {
        // given
        final Policy policy = entityManager.persist(new Policy(null, "프리셋"));
        persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findByPolicyIdAndVersionNo(Long.MAX_VALUE, 1);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 버전_목록은_해당_규칙만_버전_번호_내림차순으로_조회한다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final Policy otherPolicy = entityManager.persist(new Policy(member, "다른 규칙"));
        final PolicyVersion first = persistVersion(policy, 1);
        final PolicyVersion second = persistVersion(policy, 2);
        final PolicyVersion third = persistVersion(policy, 3);
        persistVersion(otherPolicy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final List<PolicyVersion> found = policyVersionRepository.findMemberVersions(policy.getId(), member.getId());

        // then
        assertThat(found).extracting(PolicyVersion::getId)
                .containsExactly(third.getId(), second.getId(), first.getId());
    }

    @Test
    void 다른_회원의_버전_목록은_조회하지_않는다() {
        // given
        final Member owner = entityManager.persist(new Member("owner@example.com", "encoded-password", "owner"));
        final Member other = entityManager.persist(new Member("other@example.com", "encoded-password", "other"));
        final Policy policy = entityManager.persist(new Policy(owner, "주인 규칙"));
        persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final List<PolicyVersion> found = policyVersionRepository.findMemberVersions(policy.getId(), other.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 프리셋의_버전_목록은_내_규칙으로_조회하지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy preset = entityManager.persist(new Policy(null, "프리셋"));
        persistVersion(preset, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final List<PolicyVersion> found = policyVersionRepository.findMemberVersions(preset.getId(), member.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 없는_규칙의_버전_목록은_빈_결과를_반환한다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final List<PolicyVersion> found = policyVersionRepository.findMemberVersions(Long.MAX_VALUE, member.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 버전_목록은_배분과_정책과_부모_본문을_가져오지_않는다() {
        // given
        final Policy preset = entityManager.persist(new Policy(null, "프리셋"));
        final PolicyVersion source = persistVersion(preset, 1);
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        entityManager.persist(PolicyVersion.create(policy, source, 1, 1, 500, true, 10_000, null, List.of()));
        entityManager.flush();
        entityManager.clear();

        // when
        final List<PolicyVersion> found = policyVersionRepository.findMemberVersions(policy.getId(), member.getId());

        // then
        assertThat(found).hasSize(1);
        final PolicyVersion version = found.get(0);
        final PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(version, "allocations")).isFalse();
        assertThat(persistenceUnitUtil.isLoaded(version, "policy")).isFalse();
        assertThat(version.getParentVersion().getId()).isEqualTo(source.getId());
        assertThat(persistenceUnitUtil.isLoaded(version, "parentVersion")).isFalse();
    }

    @Test
    void 최신_버전이_있어도_선택한_과거_버전의_조건과_배분을_조회한다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final List<Instrument> instruments = instrumentRepository.findAllByOrderByNameAsc();
        final Instrument firstEtf = instruments.get(0);
        final Instrument secondEtf = instruments.get(1);
        final PolicyVersion first = entityManager.persist(PolicyVersion.create(policy, null, 1, 3, 300, false, 1000, null, List.of(
                new AllocationDraft("첫 자산군", firstEtf, 6000),
                new AllocationDraft("둘째 자산군", secondEtf, 3000)
        )));
        persistVersion(policy, 2);
        entityManager.flush();
        entityManager.clear();

        // when
        final PolicyVersion found = policyVersionRepository.findMemberVersion(policy.getId(), first.getId(), member.getId())
                .orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(first.getId());
        assertThat(found.getVersionNo()).isEqualTo(1);
        assertThat(found.getReviewIntervalMonths()).isEqualTo(3);
        assertThat(found.getThresholdBps()).isEqualTo(300);
        assertThat(found.isAllowSell()).isFalse();
        assertThat(found.getCashTargetBps()).isEqualTo(1000);
        assertThat(found.getAllocations())
                .extracting(allocation -> allocation.getInstrument().getId(), PolicyAllocation::getTargetBps)
                .containsExactly(
                        tuple(firstEtf.getId(), 6000),
                        tuple(secondEtf.getId(), 3000)
                );
    }

    @Test
    void 같은_회원이어도_정책과_버전이_다르면_상세를_조회하지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final Policy otherPolicy = entityManager.persist(new Policy(member, "다른 규칙"));
        final PolicyVersion version = persistVersion(policy, 1);
        persistVersion(otherPolicy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findMemberVersion(otherPolicy.getId(), version.getId(), member.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 다른_회원의_버전_상세는_조회하지_않는다() {
        // given
        final Member owner = entityManager.persist(new Member("owner@example.com", "encoded-password", "owner"));
        final Member other = entityManager.persist(new Member("other@example.com", "encoded-password", "other"));
        final Policy policy = entityManager.persist(new Policy(owner, "주인 규칙"));
        final PolicyVersion version = persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findMemberVersion(policy.getId(), version.getId(), other.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 프리셋의_버전_상세는_내_규칙으로_조회하지_않는다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy preset = entityManager.persist(new Policy(null, "프리셋"));
        final PolicyVersion version = persistVersion(preset, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> found = policyVersionRepository.findMemberVersion(preset.getId(), version.getId(), member.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 없는_정책이나_버전이면_상세_조회_결과가_없다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final PolicyVersion version = persistVersion(policy, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        final Optional<PolicyVersion> missingVersion = policyVersionRepository.findMemberVersion(policy.getId(), Long.MAX_VALUE, member.getId());
        final Optional<PolicyVersion> missingPolicy = policyVersionRepository.findMemberVersion(Long.MAX_VALUE, version.getId(), member.getId());

        // then
        assertThat(missingVersion).isEmpty();
        assertThat(missingPolicy).isEmpty();
    }

    @Test
    void 배분이_없는_전액_현금_버전도_상세를_조회한다() {
        // given
        final Member member = entityManager.persist(new Member("member@example.com", "encoded-password", "investor"));
        final Policy policy = entityManager.persist(new Policy(member, "내 규칙"));
        final PolicyVersion version = entityManager.persist(PolicyVersion.create(policy, null, 1, 1, 500, true, 10_000, null, List.of()));
        entityManager.flush();
        entityManager.clear();

        // when
        final PolicyVersion found = policyVersionRepository.findMemberVersion(policy.getId(), version.getId(), member.getId())
                .orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(version.getId());
        assertThat(found.getCashTargetBps()).isEqualTo(10_000);
        assertThat(found.getAllocations()).isEmpty();
    }

    private PolicyVersion persistVersion(final Policy policy, final int versionNo) {
        final Instrument instrument = instrumentRepository.findAllByOrderByNameAsc().get(0);
        final PolicyVersion version = PolicyVersion.create(policy, null, versionNo, 1, 500, true, 0, null, List.of(
                new AllocationDraft("국내주식", instrument, 10_000)
        ));
        return entityManager.persist(version);
    }
}
