package com.pose.server.core.board.api;

import com.pose.server.core.board.application.BoardService;
import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.payload.BoardRequestDTO;
import com.pose.server.core.board.payload.BoardResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    /* 게시글 리스트 */
    @GetMapping("/")
    public String list(Model model) {
        List<BoardResponseDTO> boardList = boardService.getAllBoardList();
        model.addAttribute("boardList", boardList);
        return "board/list"; // /templates/board/list.html
    }

    /* 멘티 별 게시글 리스트 */


    /* 게시글 한개 view */
    @GetMapping("/{boardId}")
    public String view(@PathVariable Long boardId, Model model) {
        BoardEntity board = boardService.getBoardById(boardId);
        model.addAttribute("board", board);
        return "board/view";
    }


    /* 게시글 검색 결과 -> ajax */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        List<BoardResponseDTO> result = boardService.searchByTitle(keyword);
        model.addAttribute("boardList", result);
        return "board/list :: boardTable"; // fragment 만 반환
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
    @PostMapping
    public String create(@ModelAttribute("board") @Valid BoardRequestDTO dto) throws IOException {
        String imagePath = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String originalName = dto.getImageFile().getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path uploadPath = Paths.get("uploads/images"); // 서버 내 저장 경로
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            dto.getImageFile().transferTo(filePath.toFile());
            imagePath = "/uploads/images/" + fileName; // 웹에서 접근 가능한 경로
        }

        boardService.create(dto, imagePath);
        return "redirect:/board"; // 작성 후 경로는 고민
    }

    /* 게시글 수정 form */
    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("user");
        if(userId == null) {
            return "redirect:/members/login";
        }

        BoardResponseDTO board = boardService.getAllBoardList().stream()
                .filter(b -> b.getBoardId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
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

