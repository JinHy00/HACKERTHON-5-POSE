package com.pose.server.core.mentor.api;

import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.mentor.application.MentorApplyService;
import com.pose.server.core.mentor.payload.MentorApplyDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mentor/apply")
@RequiredArgsConstructor
public class MentorApplyController {
    private final MentorApplyService mentorApplyService;

    /**
     * [GET] 멘토 신청 폼 화면 보여주기
     * 로그인한 사용자가 폼에 접근할 수 있도록 MentorApplyDto 빈 객체를 모델에 담아 전달
     */
    @GetMapping
    public String applyForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");

        if (userId != null) {
            model.addAttribute("user", userId); // 세션에 있는 사용자 정보 전달
        }


        if (userId == null) {
            // FlashAttribute에 메시지 저장 → 로그인 페이지에서 alert로 처리
            redirectAttributes.addFlashAttribute("alert", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        model.addAttribute("mentorApplyDto", new MentorApplyDto());
        return "mentor/applyForm";
    }

    /**
     * [POST] 멘토 신청 처리
     * 로그인한 사용자 정보와 신청 폼에서 입력한 정보를 이용해 서비스 로직 호출
     *
     * @param dto       사용자 입력값 (경력, 소속 등)
     */
    @PostMapping
    public String apply(HttpSession session,
                        @ModelAttribute MentorApplyDto dto,
                        @RequestParam("resumeFile") MultipartFile resumeFile,
                        RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user");
        MemberEntity.Role role = (MemberEntity.Role) session.getAttribute("role");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/members/login";
        }

        // 이미 멘토인 경우 신청 제한
        if (role == MemberEntity.Role.MENTOR) {
            redirectAttributes.addFlashAttribute("error", "이미 멘토입니다.");
            return "redirect:/mentor/apply";
        }

        try {
            // MentorApplyDto에 파일을 설정
            dto.setResumeFile(resumeFile);  // MultipartFile로 받아서 DTO에 설정
            dto.setResumeFilename(resumeFile.getOriginalFilename());

            mentorApplyService.applyForMentor(userId, dto);
            redirectAttributes.addFlashAttribute("message", "멘토 신청이 완료되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mentor/apply";
    }


}