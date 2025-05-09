package com.pose.server.core.mentor.api;

import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.mentor.application.MentorApplyService;
import com.pose.server.core.mentor.domain.MentorApplyEntity;
import com.pose.server.core.mentor.payload.MentorApplyDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/mentor")
@RequiredArgsConstructor
public class MentorAdminController {

    private final MentorApplyService mentorApplyService;

    private boolean isAdmin(HttpSession session) {
        String userId = (String) session.getAttribute("user");
        MemberEntity.Role role = (MemberEntity.Role) session.getAttribute("role");
        return userId != null && role == MemberEntity.Role.ADMIN;
    }
    /**
     * 관리자 전용 멘토 신청 목록
     */
    @GetMapping
    public String viewMentorList(HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/members/login"; // 로그인되지 않았거나 관리자가 아니면 로그인 페이지로
        }

        // 관리자일 때만 데이터 조회 및 뷰 반환
        List<MentorApplyDto> mentorList = mentorApplyService.getAllApplications();
        model.addAttribute("mentorList", mentorList);
        return "mentor/mentorList"; // 멘토 신청 목록 페이지로 이동
    }



    @PostMapping("/{id}/approve")
    public String approveMentor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/members/login";
        }

        mentorApplyService.approveApplication(id);
        redirectAttributes.addFlashAttribute("message", "승인되었습니다.");
        return "redirect:/admin/mentor";
    }

    @PostMapping("/{id}/reject")
    public String rejectMentor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/members/login";
        }

        mentorApplyService.rejectApplication(id);
        redirectAttributes.addFlashAttribute("message", "거절되었습니다.");
        return "redirect:/admin/mentor";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).build(); // 관리자가 아니면 403 Forbidden 반환
        }

        byte[] resumeFile = mentorApplyService.downloadResume(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"") // 파일명 변경 가능 -> 파일명을 사용자가 저장한 이름 그대로 받아오게해야함
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resumeFile);
    }

    // 신청 삭제
    @PostMapping("/{id}/delete")
    public String deleteMentorApplication(@PathVariable Long id,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("alert", "접근 권한이 없습니다.");
            return "redirect:/members/login";
        }

        try {
            mentorApplyService.deleteApplication(id);
            redirectAttributes.addFlashAttribute("alert", "삭제되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());
        }
        return "redirect:/admin/mentor";
    }
}
