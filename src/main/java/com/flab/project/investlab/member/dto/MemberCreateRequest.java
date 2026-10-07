package com.flab.project.investlab.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank
        @Email(regexp = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")
        @Size(max = 255)
        String email,
        @NotBlank @Size(min = 10, max = 64) String password,
        @NotBlank
        @Size(min = 2, max = 20)
        @Pattern(regexp = "\\S(?:.*\\S)?")
        String nickname
) {
}
