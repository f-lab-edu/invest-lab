package com.flab.project.investlab.member.controller;

import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.dto.MemberCreateRequest;
import com.flab.project.investlab.member.dto.MemberResponse;
import com.flab.project.investlab.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse register(@Valid @RequestBody MemberCreateRequest request) {
        Member member = memberService.register(
                request.email(),
                request.password(),
                request.nickname()
        );
        return MemberResponse.from(member);
    }
}
