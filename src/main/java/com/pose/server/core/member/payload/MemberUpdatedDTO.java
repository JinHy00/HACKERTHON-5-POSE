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
public class MemberUpdatedDTO {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Email
    private String email;

    @NotBlank
    private String addr;

    @NotBlank
    private String tel;
}
