package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResidentSaveRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Integer gender;
    private Integer age;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
    private LocalDate admissionDate;
    private String medicalHistory;
    private String allergies;
    private String remark;
}
