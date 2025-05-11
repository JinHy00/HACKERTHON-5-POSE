package com.pose.server.core.member.api;

import com.pose.server.core.member.application.MemberService;
import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.member.payload.LoginDTO;
import com.pose.server.core.member.payload.MemberDTO;
import com.pose.server.core.member.payload.PwChangeDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @GetMapping("/join")
    public String showJoinForm(Model model) {
        model.addAttribute("member", new MemberDTO());
        return "member/join"; // templates/member/join.html
    }

    @PostMapping("/join")
    public String handleJoin(@ModelAttribute("member") @Valid MemberDTO memberDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "member/join";
        }

        memberService.join(memberDTO);
        return "redirect:/";
    }

    //로그인
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("login", new LoginDTO());
        return "member/login"; // templates/member/login.html
    }

    @PostMapping("/login")
    public String login(LoginDTO loginDTO, Model model, HttpSession session) {
        MemberEntity memberEntity = memberService.login(loginDTO);

        if (memberEntity.getUserId() != null) {
            // 로그인 성공 시 세션에 사용자 정보 저장 (예: userId)
            session.setAttribute("user", memberEntity.getUserId());
            session.setAttribute("role", memberEntity.getRole());

            String role = session.getAttribute("role").toString();

            if ("ADMIN".equals(role)) {
                return "redirect:/admin/mentor";
            }

            return "redirect:/loginhome"; // 로그인 후 리다이렉트할 페이지
        } else {
            // 로그인 실패 시 오류 메시지 전달
            model.addAttribute("loginError", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "member/login"; // 로그인 폼 다시 보여주기
        }
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 무효화 (로그아웃)
        return "redirect:/members/login";  // 로그인 페이지로 리다이렉트
    }

    //회원정보 수정
    @GetMapping("/update")
    public String showUpdateForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");
        String role = session.getAttribute("role").toString();

        if (userId != null) {
            model.addAttribute("user", userId); // 세션에 있는 사용자 정보 전달
            model.addAttribute("role", role);
        }

        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        // 서비스로부터 최신 MemberEntity 조회
        MemberEntity member = memberService.findByUserId(userId);

        MemberDTO memberDTO = MemberDTO.builder()
                .userId(member.getUserId())
                .name(member.getName())
                .email(member.getEmail())
                .addr(member.getAddr())
                .tel(member.getTel())
                .build();

        model.addAttribute("memberDTO", memberDTO);
        return "member/update-form";
    }

    @PostMapping("/update")
    public String updateMember(@ModelAttribute MemberDTO memberDTO, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");

        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        // 기존 회원 정보 조회
        MemberEntity member = memberService.findByUserId(userId);
        if (member == null) {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
            return "member/update-form";
        }

        // 회원 정보 업데이트
        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        member.setAddr(memberDTO.getAddr());
        member.setTel(memberDTO.getTel());

        memberService.update(member);  // 저장 로직은 서비스에 위임

        model.addAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        return "redirect:/loginhome";  // 또는 수정 완료 페이지로 이동
    }

    //패스워드 변경
    @GetMapping("/pwc")
    public String pwc(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");
        String role = session.getAttribute("role").toString();

        if (userId != null) {
            model.addAttribute("user", userId); // 세션에 있는 사용자 정보 전달
            model.addAttribute("role", role);
        }

        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }
        model.addAttribute("pwChangeDTO", new PwChangeDTO());
        return "member/password-change";
    }

    @PostMapping("/pwc")
    public String changePassword(@ModelAttribute PwChangeDTO pwChangeDTO, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");

        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        // 비밀번호 변경 요청 처리
        MemberEntity member = memberService.findByUserId(userId);

        // 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(pwChangeDTO.getCurrentPassword(), member.getPw())) {
            model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            return "member/password-change";
        }

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!pwChangeDTO.getNewPassword().equals(pwChangeDTO.getConfirmPassword())) {
            model.addAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "member/password-change";
        }

        // 새 비밀번호 암호화 후 저장
        member.setPw(passwordEncoder.encode(pwChangeDTO.getNewPassword()));
        memberService.update(member);

        return "redirect:/members/logout";  // 비밀번호 변경 후 로그아웃
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");
        String role = session.getAttribute("role").toString();

        if (userId != null) {
            model.addAttribute("user", userId); // 세션에 있는 사용자 정보 전달
            model.addAttribute("role", role);
        }

        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        // 서비스로부터 최신 MemberEntity 조회
        MemberEntity member = memberService.findByUserId(userId);

        MemberDTO memberDTO = MemberDTO.builder()
                .userId(member.getUserId())
                .name(member.getName())
                .email(member.getEmail())
                .addr(member.getAddr())
                .tel(member.getTel())
                .build();

        model.addAttribute("memberDTO", memberDTO);
        return "member/mypage";
    }
}