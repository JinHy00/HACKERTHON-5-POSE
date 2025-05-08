package com.pose.server.core.board.payload;

import com.pose.server.core.board.domain.BoardEntity;
import com.pose.server.core.board.domain.BoardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardRequestDTO {

    private Long memberId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    /* image */
    private MultipartFile imageFile;

    @NotNull
    private BoardStatus boardStatus;

    private String mentorId;

}
