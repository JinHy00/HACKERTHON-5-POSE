package com.pose.server.core.board.application;

import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.infrastructure.BoardRepository;
import com.pose.server.core.board.payload.BoardRequestDTO;
import com.pose.server.core.board.payload.BoardResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /* 게시글 작성 -> void 또는 responseDTO */
    public void create(BoardRequestDTO dto, String imagePath) {
        BoardEntity board = BoardEntity.builder()
                .memberId(dto.getMemberId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .image(imagePath) // 실제 파일 경로 저장
                .boardStatus(dto.getBoardStatus())

                .mentorId(dto.getMentorId())
                .build();

        boardRepository.save(board);
    }


    /* 게시글 목록 */
    public List<BoardResponseDTO> getAllBoardList() {
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(board -> BoardResponseDTO.builder()
                        .boardId(board.getBoardId())
                        .memberId(board.getMemberId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .image(board.getImage())
                        .mentorId(board.getMentorId())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }

    /* 게시글 한개 */
    public BoardEntity getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }



    /* 게시글 검색
    * => 어떤식으로 검색할 건지
    * */
    public List<BoardResponseDTO> searchByTitle(String keyword) {
        return boardRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(board -> BoardResponseDTO.builder()
                        .boardId(board.getBoardId())
                        .memberId(board.getMemberId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .image(board.getImage())
                        .mentorId(board.getMentorId())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }



    /* 게시글 수정 -> 비밀번호 입력할 건지 ? */
    @Transactional
    public BoardResponseDTO updateBoard(Long boardId, BoardRequestDTO dto, String imagePath) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setMentorId(dto.getMentorId());

        // 새 이미지가 있으면 덮어쓰기
        if (imagePath != null) {
            board.setImage(imagePath);
        }

        return BoardResponseDTO.builder()
                .boardId(board.getBoardId())
                .memberId(board.getMemberId())
                .title(board.getTitle())
                .content(board.getContent())
                .image(board.getImage())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .mentorId(board.getMentorId())
                .build();
    }




    /* 게시글 삭제 => 댓글도 삭제*/
    @Transactional
    public void deleteBoard(Long boardId){
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        boardRepository.delete(board);
    }
}
