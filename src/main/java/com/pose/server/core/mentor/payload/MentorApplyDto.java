package com.pose.server.core.mentor.payload;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorApplyDto {
    private Long applyId;

    private String mentorCareer;     // 멘토 경력 (ex: "백엔드 개발자 3년")

    private String affiliation;      // 소속 (ex: "카카오 엔터프라이즈")

    private String userId;
    private String name;
    private String email;
    private String tel;
    private String status;
    private String statusText; // 뷰에 보여줄 한글 상태
    private LocalDateTime createdAt;

    // 이력서 파일 데이터와 파일명 추가
    private MultipartFile resumeFile;  // 이력서 파일
    private String resumeFilename;  // 파일명
    private String resumeDownloadUrl; // 다운로드 URL (클릭 시 다운로드)
}