package com.pose.server.core.board.application;

import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.domain.BoardStatus;
import com.pose.server.core.board.infrastructure.BoardRepository;
import com.pose.server.core.board.payload.BoardRequestDTO;
import com.pose.server.core.board.payload.BoardResponseDTO;
import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.member.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    /* 게시글 작성 -> void 또는 responseDTO */
    public void create(BoardRequestDTO dto, String imagePath) {
        MemberEntity member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardEntity board = BoardEntity.builder()
                .memberEntity(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .image(imagePath) // 실제 파일 경로 저장
                .boardStatus(dto.getBoardStatus())

                .mentorId(dto.getMentorId())
                .build();

        boardRepository.save(board);
    }


    /* 자유 게시글 목록 */
    public Page<BoardResponseDTO> getPagedBoardList(Pageable pageable) {
        return boardRepository.findByBoardStatus(BoardStatus.FREE, pageable)
                .map(board -> BoardResponseDTO.builder()
                        .boardId(board.getBoardId())
                        .memberId(board.getMemberEntity().getMemberId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .image(board.getImage())
                        .mentorId(board.getMentorId())
                        .boardStatus(board.getBoardStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build());
    }




    /* 게시글 한개 */
    public BoardResponseDTO getBoardById(Long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return BoardResponseDTO.builder()
                .boardId(board.getBoardId())
                .memberId(board.getMemberEntity().getMemberId())
                .title(board.getTitle())
                .content(board.getContent())
                .image(board.getImage())
                .mentorId(board.getMentorId())
                .boardStatus(board.getBoardStatus())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    /* 멘티 별 게시글 리스트 */
    public List<BoardResponseDTO> findByMemberAndStatus(Long memberId, String status) {
        List<BoardEntity> boards;

        if ("FREE".equalsIgnoreCase(status)) {
            boards = boardRepository.findByMemberEntity_MemberIdAndBoardStatus(memberId, BoardStatus.FREE);
        } else if ("PERSONAL".equalsIgnoreCase(status)) {
            boards = boardRepository.findByMemberEntity_MemberIdAndBoardStatus(memberId, BoardStatus.PERSONAL);
        } else {
            boards = boardRepository.findByMemberEntity_MemberId(memberId);
        }

        return boards.stream()
                .map(board -> BoardResponseDTO.builder()
                        .boardId(board.getBoardId())
                        .memberId(board.getMemberEntity().getMemberId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .image(board.getImage())
                        .mentorId(board.getMentorId())
                        .boardStatus(board.getBoardStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .toList();
    }





    /* 게시글 검색
    * => 어떤식으로 검색할 건지
    * */
    public Page<BoardResponseDTO> searchByTitle(String keyword, Pageable pageable) {
        return boardRepository.findByTitleContainingIgnoreCaseAndBoardStatus(
                        keyword, BoardStatus.FREE, pageable)
                .map(board -> BoardResponseDTO.builder()
                        .boardId(board.getBoardId())
                        .memberId(board.getMemberEntity().getMemberId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .image(board.getImage())
                        .mentorId(board.getMentorId())
                        .boardStatus(board.getBoardStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build());
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
                .memberId(board.getMemberEntity().getMemberId())
                .title(board.getTitle())
                .content(board.getContent())
                .image(board.getImage())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .boardStatus(board.getBoardStatus())
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
