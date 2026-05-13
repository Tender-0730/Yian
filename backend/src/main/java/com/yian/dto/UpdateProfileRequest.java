package com.yian.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer gender;
}
