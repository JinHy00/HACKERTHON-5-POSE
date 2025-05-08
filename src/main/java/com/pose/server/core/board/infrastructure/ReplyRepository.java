package com.pose.server.core.board.infrastructure;

import com.pose.server.core.board.domain.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<ReplyEntity, Long> {
}
