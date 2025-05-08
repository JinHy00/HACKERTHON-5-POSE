package com.pose.server.core.board.payload;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyRequestDTO {

    private Long replyId;

    @NotBlank
    private String replyContent;

    private Long memberId;

    private Long boardId;
}
