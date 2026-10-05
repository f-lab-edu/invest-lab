package com.flab.project.investlab.member.dto;

import com.flab.project.investlab.member.domain.Member;

public record MemberResponse(
        Long id,
        String email,
        String nickname
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
