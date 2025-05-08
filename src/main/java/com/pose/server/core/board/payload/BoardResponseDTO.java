package com.pose.server.core.board.payload;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponseDTO {

    private Long boardId;

    private Long memberId;

    private String title;

    private String content;

    private String image;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String mentorId;
}

