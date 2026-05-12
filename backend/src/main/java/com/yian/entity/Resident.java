package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resident")
public class Resident {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Integer gender;
    private Integer age;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String avatar;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private String status;
    private String medicalHistory;
    private String allergies;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
