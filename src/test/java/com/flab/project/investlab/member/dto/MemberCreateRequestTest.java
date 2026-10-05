package com.flab.project.investlab.member.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberCreateRequestTest {

    @Test
    void 회원가입_요청은_이메일과_닉네임의_공백을_제거한다() {
        // When
        MemberCreateRequest request = new MemberCreateRequest(
                " member@example.com ",
                "password1234",
                " investor "
        );

        // Then
        assertThat(request.email()).isEqualTo("member@example.com");
        assertThat(request.nickname()).isEqualTo("investor");
    }
}
