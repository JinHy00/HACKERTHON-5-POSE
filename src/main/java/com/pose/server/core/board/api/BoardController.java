package com.pose.server.core.board.api;

import com.pose.server.core.board.application.BoardService;
import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.payload.BoardRequestDTO;
import com.pose.server.core.board.payload.BoardResponseDTO;
import com.pose.server.core.member.application.MemberService;
import com.pose.server.core.member.domain.MemberEntity;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;


    /* 자유 게시판 리스트 */
    @GetMapping("")
    public String list(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<BoardResponseDTO> boardPage = boardService.getPagedBoardList(pageable);
        model.addAttribute("boardPage", boardPage);
        return "board/list";
    }

    /* 멘티 별 게시글 리스트
      * 멘티 마이페이지에서 뜨도록
     */
    @GetMapping("/mentee")
    public String menteePage(@RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                             HttpSession session,
                             Model model) {

        String userId = (String) session.getAttribute("user");
        if (userId == null) return "redirect:/members/login";

        Long memberId = memberService.findByUserId(userId).getMemberId();

        // 멘티 정보
        model.addAttribute("mentee", memberService.findByUserId(userId));

        // 초기 목록
        List<BoardResponseDTO> boardList = boardService.findByMemberAndStatus(memberId, status);
        model.addAttribute("boardList", boardList);
        model.addAttribute("currentStatus", status);

        return "board/mentee-page";
    }

    @GetMapping("/mentee/filter")
    public String filterMenteeBoard(@RequestParam String status,
                                    HttpSession session,
                                    Model model) {
        String userId = (String) session.getAttribute("user");
        if (userId == null) return "redirect:/members/login";

        Long memberId = memberService.findByUserId(userId).getMemberId();

        List<BoardResponseDTO> boardList = boardService.findByMemberAndStatus(memberId, status);
        model.addAttribute("boardList", boardList);

        return "board/mentee-page :: boardTable";
    }




    /* 멘토 별 게시글 리스트 (1:1 게시판 리스트)
    * 멘토 마이페이지에서 뜨도록?
    * */


    /* 게시글 한개 view */
    @GetMapping("/{boardId}")
    public String view(@PathVariable Long boardId, Model model, HttpSession session) {
        BoardResponseDTO board = boardService.getBoardById(boardId);
        model.addAttribute("board", board);

        // 세션 유저 ID 추가
        String userId = (String) session.getAttribute("user");
        model.addAttribute("userId", userId);

        return "board/view";
    }


    /* 게시글 검색 결과 -> ajax */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {
        Page<BoardResponseDTO> resultPage = boardService.searchByTitle(keyword, pageable);
        model.addAttribute("boardPage", resultPage); // boardList → boardPage
        model.addAttribute("keyword", keyword); // 검색어 유지용
        return "board/list :: boardTable";
    }


    /* 게시글 작성 페이지
    * userId null 처리 할 클래스? 필요
    * */
    @GetMapping("/create")
    public String createForm(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if(userId == null) {
            return "redirect:/members/login";
        }
        model.addAttribute("board", new BoardRequestDTO());
        return "board/create";
    }

    /* 게시글 작성 */
    @PostMapping("/create")
    public String create(@ModelAttribute("board") @Valid BoardRequestDTO dto, HttpSession session) throws IOException {
        String userId = (String) session.getAttribute("user");
        if(userId == null) {
            return "redirect:/members/login";
        }
        log.info("userID:{}",userId);
        /* userId 로 memberId setting */
        Long memberId = memberService.findByUserId(userId).getMemberId();
        // Long memberId = memberService.getMemberIdByUserId(userId);
        log.info("memberId:{}",memberId);
        dto.setMemberId(memberId);

        String imagePath = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String originalName = dto.getImageFile().getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "images");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            dto.getImageFile().transferTo(filePath.toFile());
            imagePath = "/uploads/images/" + fileName; // 웹에서 접근 가능한 경로
        }

        boardService.create(dto, imagePath);
        return "redirect:/board"; // 작성 후 경로는 고민
    }

    /* 게시글 수정 form
    -> edit 페이지로 안가고 view(게시글 한개) 에서 바로 수정하게 할 지?
     -> session 으로 userid 확인해서 일치하면 input 수정할 수 있게?
     */
    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if(userId == null) {
            return "redirect:/members/login";
        }

        BoardResponseDTO board = boardService.getBoardById(boardId);
        model.addAttribute("board", board);
        return "board/edit";
    }

    /* 게시글 수정 처리 */
    @PostMapping("/edit/{boardId}")
    public String edit(@PathVariable Long boardId, @ModelAttribute("board") @Valid BoardRequestDTO dto) throws IOException {
        String imagePath = null;

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String originalName = dto.getImageFile().getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path uploadPath = Paths.get("uploads/images");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            dto.getImageFile().transferTo(filePath.toFile());
            imagePath = "/uploads/images/" + fileName;
        }

        boardService.updateBoard(boardId, dto, imagePath);
        return "redirect:/board";
    }

    /* 게시글 삭제 */
    @PostMapping("/delete/{boardId}")
    public String delete(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return "redirect:/board";
    }
}

