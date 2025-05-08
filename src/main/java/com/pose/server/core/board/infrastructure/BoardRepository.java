package com.pose.server.core.board.infrastructure;

import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.domain.BoardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    /* title 로 게시물 검색*/
    Page<BoardEntity> findByTitleContainingIgnoreCaseAndBoardStatus(String keyword, BoardStatus boardStatus, Pageable pageable);

    /* 자유게시판 리스트 */
    Page<BoardEntity> findByBoardStatus(BoardStatus boardStatus, Pageable pageable);


    // 멤버 ID + 상태로 조회
    List<BoardEntity> findByMemberEntity_MemberIdAndBoardStatus(Long memberId, BoardStatus boardStatus);

    // 멤버 ID로 전체 조회 (상태 무관)
    List<BoardEntity> findByMemberEntity_MemberId(Long memberId);

}
