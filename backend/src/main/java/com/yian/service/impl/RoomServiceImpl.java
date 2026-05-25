package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yian.common.BusinessException;
import com.yian.common.ResultCode;
import com.yian.dto.CheckInRequest;
import com.yian.dto.RoomSaveRequest;
import com.yian.entity.*;
import com.yian.enums.BedStatusEnum;
import com.yian.enums.CheckInStatusEnum;
import com.yian.enums.ResidentStatusEnum;
import com.yian.mapper.*;
import com.yian.service.RoomService;
import com.yian.vo.BedVO;
import com.yian.vo.BuildingVO;
import com.yian.vo.RoomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final ResidentMapper residentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BuildingVO> listBuildings() {
        List<Building> buildings = buildingMapper.selectList(
                new LambdaQueryWrapper<Building>().orderByAsc(Building::getId));
        return buildings.stream().map(b -> BuildingVO.builder()
                .id(b.getId())
                .buildingName(b.getBuildingName())
                .floorCount(b.getFloorCount())
                .description(b.getDescription())
                .build()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomVO> listRoomsByBuilding(Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        List<Room> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>()
                        .eq(Room::getBuildingId, buildingId)
                        .orderByAsc(Room::getRoomNumber));
        return rooms.stream().map(r -> RoomVO.builder()
                .id(r.getId())
                .buildingId(r.getBuildingId())
                .buildingName(building != null ? building.getBuildingName() : null)
                .floor(r.getFloor())
                .roomNumber(r.getRoomNumber())
                .roomType(r.getRoomType())
                .capacity(r.getCapacity())
                .occupied(r.getOccupied())
                .status(r.getStatus())
                .description(r.getDescription())
                .build()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedVO> listBedsByRoom(Long roomId) {
        List<Bed> beds = bedMapper.selectByRoomId(roomId);
        if (CollUtil.isEmpty(beds)) {
            return List.of();
        }

        // 查询当前入住中的记录，关联床位和老人名
        List<Long> bedIds = beds.stream().map(Bed::getId).toList();
        List<CheckInRecord> activeRecords = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .in(CheckInRecord::getBedId, bedIds)
                        .eq(CheckInRecord::getStatus, CheckInStatusEnum.CHECKED_IN.getCode()));
        Map<Long, CheckInRecord> recordMap = activeRecords.stream()
                .collect(Collectors.toMap(CheckInRecord::getBedId, r -> r, (a, b) -> a));
        Map<Long, String> residentNameMap = Map.of();
        if (!activeRecords.isEmpty()) {
            List<Long> residentIds = activeRecords.stream().map(CheckInRecord::getResidentId).distinct().toList();
            residentNameMap = residentMapper.selectBatchIds(residentIds).stream()
                    .collect(Collectors.toMap(Resident::getId, Resident::getName));
        }

        Map<Long, String> finalNameMap = residentNameMap;
        return beds.stream().map(b -> {
            CheckInRecord rec = recordMap.get(b.getId());
            return BedVO.builder()
                    .id(b.getId())
                    .roomId(b.getRoomId())
                    .bedNumber(b.getBedNumber())
                    .status(b.getStatus())
                    .recordId(rec != null ? rec.getId() : null)
                    .residentName(rec != null ? finalNameMap.getOrDefault(rec.getResidentId(), "") : null)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public Long createRoom(RoomSaveRequest request) {
        Room room = new Room();
        room.setBuildingId(request.getBuildingId());
        room.setFloor(request.getFloor());
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity() != null ? request.getCapacity() : 0);
        room.setOccupied(0);
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());
        roomMapper.insert(room);
        log.info("新增房间成功: id={}, roomNumber={}", room.getId(), room.getRoomNumber());
        return room.getId();
    }

    @Override
    @Transactional
    public void updateRoom(Long id, RoomSaveRequest request) {
        Room existing = roomMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        }
        Room room = new Room();
        room.setId(id);
        room.setBuildingId(request.getBuildingId());
        room.setFloor(request.getFloor());
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());
        roomMapper.updateById(room);
        log.info("更新房间成功: id={}", id);
    }

    /**
     * 办理入住 — 在同一事务中同步更新4张表，保证数据一致性：
     * 1. 创建入住记录
     * 2. 床位 → OCCUPIED
     * 3. 房间已占用数 +1
     * 4. 老人状态 → CHECKED_IN
     */
    @Override
    @Transactional
    public void checkIn(CheckInRequest request) {
        Resident resident = residentMapper.selectById(request.getResidentId());
        if (resident == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }

        Bed bed = bedMapper.selectById(request.getBedId());
        if (bed == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "床位不存在");
        }

        CheckInRecord record = new CheckInRecord();
        record.setResidentId(request.getResidentId());
        record.setBedId(request.getBedId());
        record.setCheckInDate(request.getCheckInDate());
        record.setStatus(CheckInStatusEnum.CHECKED_IN.getCode());
        record.setRemark(request.getRemark());
        checkInRecordMapper.insert(record);

        // 原子更新：只有空闲床位才能入住，防止并发重复入住
        int rows = bedMapper.occupyIfAvailable(request.getBedId());
        if (rows == 0) {
            throw new BusinessException("该床位已被占用");
        }

        Room room = roomMapper.selectById(bed.getRoomId());
        Room updateRoom = new Room();
        updateRoom.setId(room.getId());
        updateRoom.setOccupied(room.getOccupied() + 1);
        roomMapper.updateById(updateRoom);

        Resident updateResident = new Resident();
        updateResident.setId(request.getResidentId());
        updateResident.setStatus(ResidentStatusEnum.CHECKED_IN.getCode());
        residentMapper.updateById(updateResident);

        log.info("办理入住成功: residentId={}, bedId={}", request.getResidentId(), request.getBedId());
    }

    /**
     * 办理退住 — 入住的逆操作，同步回滚4张表：
     * 1. 入住记录 → CHECKED_OUT（记录退住日期）
     * 2. 床位 → AVAILABLE
     * 3. 房间已占用数 -1（防负数兜底）
     * 4. 老人状态 → CHECKED_OUT
     */
    @Override
    @Transactional
    public void checkOut(Long recordId) {
        CheckInRecord record = checkInRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "入住记录不存在");
        }
        // 只有入住中的记录才能退住，防止重复退住
        if (!CheckInStatusEnum.CHECKED_IN.getCode().equals(record.getStatus())) {
            throw new BusinessException("该记录已退住");
        }

        record.setStatus(CheckInStatusEnum.CHECKED_OUT.getCode());
        record.setCheckOutDate(LocalDate.now());
        checkInRecordMapper.updateById(record);

        Bed updateBed = new Bed();
        updateBed.setId(record.getBedId());
        updateBed.setStatus(BedStatusEnum.AVAILABLE.getCode());
        bedMapper.updateById(updateBed);

        Bed bed = bedMapper.selectById(record.getBedId());
        Room room = roomMapper.selectById(bed.getRoomId());
        Room updateRoom = new Room();
        updateRoom.setId(room.getId());
        // Math.max 防止并发或数据异常时 occupied 出现负数
        updateRoom.setOccupied(Math.max(0, room.getOccupied() - 1));
        roomMapper.updateById(updateRoom);

        Resident updateResident = new Resident();
        updateResident.setId(record.getResidentId());
        updateResident.setStatus(ResidentStatusEnum.CHECKED_OUT.getCode());
        residentMapper.updateById(updateResident);

        log.info("办理退住成功: recordId={}, residentId={}", recordId, record.getResidentId());
    }
}
