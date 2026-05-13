package com.yian.service;

import com.yian.dto.CheckInRequest;
import com.yian.dto.RoomSaveRequest;
import com.yian.entity.Building;
import com.yian.vo.BedVO;
import com.yian.vo.RoomVO;

import java.util.List;

public interface RoomService {

    List<Building> listBuildings();

    List<RoomVO> listRoomsByBuilding(Long buildingId);

    List<BedVO> listBedsByRoom(Long roomId);

    Long createRoom(RoomSaveRequest request);

    void updateRoom(Long id, RoomSaveRequest request);

    void checkIn(CheckInRequest request);

    void checkOut(Long recordId);
}
