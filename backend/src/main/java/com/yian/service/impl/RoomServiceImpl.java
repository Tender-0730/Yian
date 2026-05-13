package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yian.common.BusinessException;
import com.yian.common.ResultCode;
import com.yian.dto.CheckInRequest;
import com.yian.dto.RoomSaveRequest;
import com.yian.entity.*;
import com.yian.enums.ResidentStatusEnum;
import com.yian.mapper.*;
import com.yian.service.RoomService;
import com.yian.vo.BedVO;
import com.yian.vo.RoomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
    public List<Building> listBuildings() {
        return buildingMapper.selectList(
                new LambdaQueryWrapper<Building>().orderByAsc(Building::getId));
    }

    @Override
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
    public List<BedVO> listBedsByRoom(Long roomId) {
        List<Bed> beds = bedMapper.selectByRoomId(roomId);
        return beds.stream().map(b -> BedVO.builder()
                .id(b.getId())
                .roomId(b.getRoomId())
                .bedNumber(b.getBedNumber())
                .status(b.getStatus())
                .build()).toList();
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
        if (!"AVAILABLE".equals(bed.getStatus())) {
            throw new BusinessException("该床位已被占用");
        }

        CheckInRecord record = new CheckInRecord();
        record.setResidentId(request.getResidentId());
        record.setBedId(request.getBedId());
        record.setCheckInDate(request.getCheckInDate());
        record.setStatus("CHECKED_IN");
        record.setRemark(request.getRemark());
        checkInRecordMapper.insert(record);

        Bed updateBed = new Bed();
        updateBed.setId(request.getBedId());
        updateBed.setStatus("OCCUPIED");
        bedMapper.updateById(updateBed);

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

    @Override
    @Transactional
    public void checkOut(Long recordId) {
        CheckInRecord record = checkInRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "入住记录不存在");
        }
        if (!"CHECKED_IN".equals(record.getStatus())) {
            throw new BusinessException("该记录已退住");
        }

        record.setStatus("CHECKED_OUT");
        record.setCheckOutDate(LocalDate.now());
        checkInRecordMapper.updateById(record);

        Bed updateBed = new Bed();
        updateBed.setId(record.getBedId());
        updateBed.setStatus("AVAILABLE");
        bedMapper.updateById(updateBed);

        Bed bed = bedMapper.selectById(record.getBedId());
        Room room = roomMapper.selectById(bed.getRoomId());
        Room updateRoom = new Room();
        updateRoom.setId(room.getId());
        updateRoom.setOccupied(Math.max(0, room.getOccupied() - 1));
        roomMapper.updateById(updateRoom);

        Resident updateResident = new Resident();
        updateResident.setId(record.getResidentId());
        updateResident.setStatus(ResidentStatusEnum.CHECKED_OUT.getCode());
        residentMapper.updateById(updateResident);

        log.info("办理退住成功: recordId={}, residentId={}", recordId, record.getResidentId());
    }
}
