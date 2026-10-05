package com.flab.project.investlab.member.service;

import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.exception.MemberErrorCode;
import com.flab.project.investlab.member.exception.MemberException;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Member register(String email, String rawPassword, String nickname) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedNickname = nickname.trim();

        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
        }
        if (memberRepository.existsByNickname(normalizedNickname)) {
            throw new MemberException(MemberErrorCode.NICKNAME_DUPLICATED);
        }

        Member member = new Member(
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                normalizedNickname
        );
        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException exception) {
            throw new MemberException(MemberErrorCode.MEMBER_DUPLICATED, exception);
        }
    }
}
