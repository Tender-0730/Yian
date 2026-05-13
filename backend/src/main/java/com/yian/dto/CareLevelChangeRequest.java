package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CareLevelChangeRequest {
    @NotNull(message = "护理级别ID不能为空")
    private Long careLevelId;
}
