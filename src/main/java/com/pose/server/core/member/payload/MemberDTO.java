package com.pose.server.core.member.payload;

import jakarta.validation.constraints.Email;
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
public class MemberDTO {

    @NotBlank
    @Size(max = 30)
    private String userId;

    @NotBlank
    @Size(max = 200)
    private String pw;

    @NotBlank
    @Size(max = 50)
    private String name;

    private String role; // 문자열로 받고 enum으로 변환

    @Email
    private String email;

    @NotBlank
    private String addr;

    @NotBlank
    private String tel;

}