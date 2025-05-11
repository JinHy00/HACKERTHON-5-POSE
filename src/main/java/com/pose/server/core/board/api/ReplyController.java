package com.pose.server.core.board.api;

import com.pose.server.core.board.application.ReplyService;
import com.pose.server.core.board.payload.ReplyRequestDTO;
import com.pose.server.core.board.payload.ReplyResponseDTO;
import com.pose.server.core.member.application.MemberService;
import com.pose.server.core.member.infrastructure.MemberRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reply")
public class ReplyController {

    private final ReplyService replyService;
    private final MemberService memberService;

    /* 댓글 작성 */
    @PostMapping("/create")
    public String createReply(@ModelAttribute @Valid ReplyRequestDTO dto, HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if (userId == null) return "redirect:/members/login";

        /*memberId 설정*/
        Long memberId = memberService.findByUserId(userId).getMemberId();
        dto.setMemberId(memberId);

        replyService.createReply(dto);
        return "redirect:/board/" + dto.getBoardId();
    }

    /* 수정*/
    @PostMapping("/update/{replyId}")
    public String updateReply(@PathVariable Long replyId,
                              @RequestParam String replyContent,
                              @RequestParam Long boardId
                              ) {

        replyService.updateReply(replyId, replyContent);
        return "redirect:/board/" + boardId;
    }

    /* 삭제 */
    @PostMapping("/delete/{replyId}")
    public String deleteReply(@PathVariable Long replyId, @RequestParam Long boardId) {
        replyService.deleteReply(replyId);
        return "redirect:/board/" + boardId;
    }

    /* 게시글 별 댓글 리스트 */
    @GetMapping("/list/{boardId}")
    public String getReplies(@PathVariable Long boardId, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if (userId == null) return "redirect:/members/login";

        model.addAttribute("user", userId);

        List<ReplyResponseDTO> replies = replyService.getRepliesByBoardId(boardId);
        model.addAttribute("replies", replies);
        return "reply/list :: replyList"; // fragment 반환용
    }
}
