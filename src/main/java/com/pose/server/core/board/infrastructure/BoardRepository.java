package com.pose.server.core.board.infrastructure;

import com.pose.server.core.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    /* title 로 게시물 검색*/
    List<BoardEntity> findByTitleContainingIgnoreCase(String keyword);
}
