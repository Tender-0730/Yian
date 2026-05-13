package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ResidentDetailVO {
    private Long id;
    private String name;
    private Integer gender;
    private Integer age;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String status;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
    private String medicalHistory;
    private String allergies;
    private String remark;
    private String careLevelName;
    private Long careLevelId;
    private String roomNumber;
    private String bedNumber;
    private Long bedId;
    private LocalDateTime createdAt;
}
