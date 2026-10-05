package com.flab.project.investlab.member.service;

import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.exception.MemberErrorCode;
import com.flab.project.investlab.member.exception.MemberException;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        // Given
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.register("  MEMBER@Example.COM ", "password1234", " investor ");

        // Then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        Member savedMember = captor.getValue();
        assertThat(savedMember.getEmail()).isEqualTo("member@example.com");
        assertThat(savedMember.getNickname()).isEqualTo("investor");
        assertThat(passwordEncoder.matches("password1234", savedMember.getPasswordHash())).isTrue();
    }

    @Test
    void 중복된_이메일로_회원가입하면_예외가_발생한다() {
        // Given
        when(memberRepository.existsByEmail("member@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.EMAIL_DUPLICATED.getCode());
    }

    @Test
    void 중복된_닉네임으로_회원가입하면_예외가_발생한다() {
        // Given
        when(memberRepository.existsByNickname("investor")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(MemberErrorCode.NICKNAME_DUPLICATED.getCode());
    }

    @Test
    void 회원_ID로_현재_회원을_조회한다() {
        // Given
        Member member = new Member("member@example.com", "encoded-password", "investor");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When
        Member foundMember = memberService.getById(1L);

        // Then
        assertThat(foundMember).isSameAs(member);
    }

    @Test
    void 존재하지_않는_회원_ID를_조회하면_예외가_발생한다() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.getById(1L))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> {
                    MemberException memberException = (MemberException) exception;
                    assertThat(memberException.getErrorCode()).isEqualTo("MEMBER_NOT_FOUND");
                    assertThat(memberException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }
}
