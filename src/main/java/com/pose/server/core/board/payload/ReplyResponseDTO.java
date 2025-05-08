package com.pose.server.core.board.payload;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyResponseDTO {

    private Long replyId;

    private String replyContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long memberId;

    private String userId;

    private String name;
}
