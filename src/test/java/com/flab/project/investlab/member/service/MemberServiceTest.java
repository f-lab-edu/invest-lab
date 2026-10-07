package com.flab.project.investlab.member.service;

import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.exception.MemberErrorCode;
import com.flab.project.investlab.member.exception.MemberException;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberServiceTest {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final MemberService memberService = new MemberService(memberRepository, passwordEncoder);

    @Test
    void 회원가입하면_이메일을_정규화하고_비밀번호를_암호화한다() {
        // when
        memberService.register("MEMBER@Example.COM", "password1234", "investor");

        // then
        final ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        final Member savedMember = captor.getValue();
        assertThat(savedMember.getEmail()).isEqualTo("member@example.com");
        assertThat(savedMember.getNickname()).isEqualTo("investor");
        assertThat(passwordEncoder.matches("password1234", savedMember.getPasswordHash())).isTrue();
    }

    @Test
    void 중복된_이메일로_회원가입하면_예외가_발생한다() {
        // given
        when(memberRepository.existsByEmail("member@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.EMAIL_DUPLICATED.getCode());
    }

    @Test
    void 중복된_닉네임으로_회원가입하면_예외가_발생한다() {
        // given
        when(memberRepository.existsByNickname("investor")).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.NICKNAME_DUPLICATED.getCode());
    }

    @Test
    void 비밀번호가_72바이트를_초과하면_회원가입을_거부한다() {
        // given
        final String password = "가".repeat(25);

        // when & then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", password, "investor")
        )
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.PASSWORD_TOO_LONG.getCode());
    }

    @Test
    void 비밀번호가_72바이트이면_회원가입을_허용한다() {
        // given
        final String password = "가".repeat(24);

        // when
        memberService.register("member@example.com", password, "investor");

        // then
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void 저장소_무결성_예외는_회원_예외로_변환한다() {
        // given
        final DataIntegrityViolationException cause = new DataIntegrityViolationException("duplicate");
        when(memberRepository.save(any(Member.class))).thenThrow(cause);

        // when & then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(MemberException.class)
                .hasCause(cause)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_DUPLICATED.getCode());
    }

    @Test
    void 회원_ID로_현재_회원을_조회한다() {
        // given
        final Member member = new Member("member@example.com", "encoded-password", "investor");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        final Member foundMember = memberService.getById(1L);

        // then
        assertThat(foundMember).isSameAs(member);
    }

    @Test
    void 존재하지_않는_회원_ID를_조회하면_예외가_발생한다() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getById(1L))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> {
                    final MemberException memberException = (MemberException) exception;
                    assertThat(memberException.getErrorCode()).isEqualTo("MEMBER_NOT_FOUND");
                    assertThat(memberException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }
}
