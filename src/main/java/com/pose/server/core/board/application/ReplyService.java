package com.pose.server.core.board.application;

import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.domain.ReplyEntity;
import com.pose.server.core.board.infrastructure.BoardRepository;
import com.pose.server.core.board.infrastructure.ReplyRepository;
import com.pose.server.core.board.payload.ReplyRequestDTO;
import com.pose.server.core.board.payload.ReplyResponseDTO;
import com.pose.server.core.member.domain.MemberEntity;
import com.pose.server.core.member.infrastructure.MemberRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    /*댓글 생성*/
    @Transactional
    public void createReply(ReplyRequestDTO dto) {
        MemberEntity member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        BoardEntity board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        ReplyEntity reply = ReplyEntity.builder()
                .replyContent(dto.getReplyContent())
                .memberEntity(member)
                .boardEntity(board)
                .build();

        replyRepository.save(reply);
    }

    /*
    * 게시글 별 댓글 리스트 */
    public List<ReplyResponseDTO> getRepliesByBoardId(Long boardId) {
        return replyRepository.findAll().stream()
                .filter(reply -> reply.getBoardEntity().getBoardId().equals(boardId))
                .map(reply -> ReplyResponseDTO.builder()
                        .replyId(reply.getReplyId())
                        .replyContent(reply.getReplyContent())
                        .createdAt(reply.getCreatedAt())
                        .updatedAt(reply.getUpdatedAt())
                        .userId(reply.getMemberEntity().getUserId())
                        .name(reply.getMemberEntity().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateReply(Long replyId, String newContent) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));


        reply.setReplyContent(newContent);
    }

    @Transactional
    public void deleteReply(Long replyId) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        replyRepository.delete(reply);
    }
}
