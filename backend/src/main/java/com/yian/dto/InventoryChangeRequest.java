package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InventoryChangeRequest {
    @NotNull(message = "库存ID不能为空")
    private Long id;

    @Positive(message = "数量必须大于0")
    private int quantity;

    private String reason;
}
