package com.flab.project.investlab.member.service;

import com.flab.project.investlab.common.error.ApiException;
import com.flab.project.investlab.common.error.ErrorCode;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void register_normalizesEmailAndHashesPassword() {
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
    void register_rejectsDuplicatedEmail() {
        // Given
        when(memberRepository.existsByEmail("member@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_EMAIL_DUPLICATED);
    }

    @Test
    void register_rejectsDuplicatedNickname() {
        // Given
        when(memberRepository.existsByNickname("investor")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() ->
                memberService.register("member@example.com", "password1234", "investor")
        )
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
    }
}
