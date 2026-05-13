package com.yian.controller;

import com.yian.common.Result;
import com.yian.dto.CheckInRequest;
import com.yian.dto.RoomSaveRequest;
import com.yian.service.RoomService;
import com.yian.vo.BedVO;
import com.yian.vo.BuildingVO;
import com.yian.vo.RoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "楼栋列表")
    @GetMapping("/buildings")
    public Result<List<BuildingVO>> listBuildings() {
        return Result.success(roomService.listBuildings());
    }

    @Operation(summary = "楼栋房间列表")
    @GetMapping("/buildings/{id}/rooms")
    public Result<List<RoomVO>> listRoomsByBuilding(@PathVariable Long id) {
        return Result.success(roomService.listRoomsByBuilding(id));
    }

    @Operation(summary = "房间床位列表")
    @GetMapping("/rooms/{id}/beds")
    public Result<List<BedVO>> listBedsByRoom(@PathVariable Long id) {
        return Result.success(roomService.listBedsByRoom(id));
    }

    @Operation(summary = "新增房间")
    @PostMapping("/rooms")
    public Result<Long> createRoom(@Valid @RequestBody RoomSaveRequest request) {
        return Result.success(roomService.createRoom(request));
    }

    @Operation(summary = "编辑房间")
    @PutMapping("/rooms/{id}")
    public Result<Void> updateRoom(@PathVariable Long id,
                                    @Valid @RequestBody RoomSaveRequest request) {
        roomService.updateRoom(id, request);
        return Result.success();
    }

    @Operation(summary = "办理入住")
    @PostMapping("/check-in")
    public Result<Void> checkIn(@Valid @RequestBody CheckInRequest request) {
        roomService.checkIn(request);
        return Result.success();
    }

    @Operation(summary = "办理退住")
    @PostMapping("/check-out/{recordId}")
    public Result<Void> checkOut(@PathVariable Long recordId) {
        roomService.checkOut(recordId);
        return Result.success();
    }
}
