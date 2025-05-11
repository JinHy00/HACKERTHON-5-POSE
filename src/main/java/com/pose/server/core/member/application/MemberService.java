package com.pose.server.core.member.application;

import com.pose.server.core.config.SecurityConfig;
import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.member.infrastructure.MemberRepository;
import com.pose.server.core.member.payload.LoginDTO;
import com.pose.server.core.member.payload.MemberDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(MemberDTO memberDTO) {
        if (memberRepository.existsByUserId(memberDTO.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(memberDTO.getPw());

        MemberEntity member = new MemberEntity();
        member.setUserId(memberDTO.getUserId());
        member.setPw(encodedPassword); // 실제 서비스에서는 반드시 암호화 필요
        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        member.setAddr(memberDTO.getAddr());
        member.setTel(memberDTO.getTel());
        member.setIsActive(true);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        member.setLastLoginAt(LocalDateTime.now());

        memberRepository.save(member);
    }

    public MemberEntity login(LoginDTO loginDTO) {
        // userId를 기준으로 MemberEntity를 찾아옴
        MemberEntity member = memberRepository.findByUserId(loginDTO.getUserId())
                .orElse(null);

        if (member == null) {
            return null; // 유저가 없으면 로그인 실패
        }

        // 비밀번호 대조
        if (passwordEncoder.matches(loginDTO.getPw(), member.getPw())) {
            return member; // 로그인 성공 시 MemberEntity 객체 반환
        } else {
            return null; // 비밀번호 불일치 시 로그인 실패
        }
    }

    public MemberEntity findByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public void update(MemberEntity member) {
        memberRepository.save(member);
    }
}