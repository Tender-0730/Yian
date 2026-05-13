package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ResidentVO {
    private Long id;
    private String name;
    private Integer gender;
    private Integer age;
    private String idCard;
    private String phone;
    private String status;
    private LocalDate admissionDate;
    private String careLevelName;
    private String roomNumber;
    private String bedNumber;
}
