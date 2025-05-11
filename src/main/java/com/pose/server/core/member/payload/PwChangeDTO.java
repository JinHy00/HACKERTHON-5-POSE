package com.pose.server.core.member.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PwChangeDTO {

    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 200, message = "비밀번호는 8자 이상 200자 이하로 입력해주세요.")
    private String newPassword;

    @NotBlank(message = "새로운 비밀번호 확인을 입력해 주세요.")
    private String confirmPassword;

    // Getters and Setters
}
