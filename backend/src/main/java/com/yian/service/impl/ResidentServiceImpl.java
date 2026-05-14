package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.ResidentPageQuery;
import com.yian.dto.ResidentSaveRequest;
import com.yian.entity.*;
import com.yian.enums.CareLevelStatusEnum;
import com.yian.enums.CheckInStatusEnum;
import com.yian.mapper.*;
import com.yian.service.ResidentService;
import com.yian.vo.CareLevelVO;
import com.yian.vo.ResidentDetailVO;
import com.yian.vo.ResidentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    private final ResidentMapper residentMapper;
    private final ResidentCareLevelMapper residentCareLevelMapper;
    private final CareLevelMapper careLevelMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ResidentVO> pageResidents(ResidentPageQuery query) {
        LambdaQueryWrapper<Resident> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getName())) {
            wrapper.like(Resident::getName, query.getName());
        }
        if (StrUtil.isNotBlank(query.getStatus())) {
            wrapper.eq(Resident::getStatus, query.getStatus());
        }

        // careLevelId 不在 Resident 表上，需先查中间表 resident_care_level 获取匹配的 residentId 列表，再 IN 查询
        if (query.getCareLevelId() != null) {
            List<Long> ids = residentCareLevelMapper.selectList(
                    new LambdaQueryWrapper<ResidentCareLevel>()
                            .eq(ResidentCareLevel::getCareLevelId, query.getCareLevelId())
                            .eq(ResidentCareLevel::getStatus, CareLevelStatusEnum.ACTIVE.getCode()))
                    .stream().map(ResidentCareLevel::getResidentId).toList();
            if (CollUtil.isEmpty(ids)) {
                return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
            }
            wrapper.in(Resident::getId, ids);
        }

        wrapper.orderByDesc(Resident::getCreatedAt);

        Page<Resident> page = new Page<>(query.getPage(), query.getSize());
        Page<Resident> result = residentMapper.selectPage(page, wrapper);
        List<Resident> residents = result.getRecords();

        if (CollUtil.isEmpty(residents)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        // 批量查出护理级别名称和床位/房间号，一次性组装到 VO，避免逐条查询（N+1）
        List<Long> residentIds = residents.stream().map(Resident::getId).toList();

        Map<Long, String> careLevelNameMap = buildCareLevelNameMap(residentIds);
        Map<Long, String> roomNumberMap = new HashMap<>();
        Map<Long, String> bedNumberMap = new HashMap<>();
        buildRoomBedMaps(residentIds, roomNumberMap, bedNumberMap);

        List<ResidentVO> vos = residents.stream().map(r -> ResidentVO.builder()
                .id(r.getId())
                .name(r.getName())
                .gender(r.getGender())
                .age(r.getAge())
                .idCard(r.getIdCard())
                .phone(r.getPhone())
                .status(r.getStatus())
                .admissionDate(r.getAdmissionDate())
                .careLevelName(careLevelNameMap.get(r.getId()))
                .roomNumber(roomNumberMap.get(r.getId()))
                .bedNumber(bedNumberMap.get(r.getId()))
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentDetailVO getResidentById(Long id) {
        Resident resident = residentMapper.selectById(id);
        if (resident == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }

        String careLevelName = null;
        Long careLevelId = null;
        ResidentCareLevel rcl = residentCareLevelMapper.selectCurrentByResidentId(id);
        if (rcl != null) {
            careLevelId = rcl.getCareLevelId();
            CareLevel cl = careLevelMapper.selectById(careLevelId);
            if (cl != null) {
                careLevelName = cl.getLevelName();
            }
        }

        String roomNumber = null;
        String bedNumber = null;
        Long bedId = null;
        CheckInRecord checkIn = checkInRecordMapper.selectCurrentByResidentId(id);
        if (checkIn != null) {
            bedId = checkIn.getBedId();
            Bed bed = bedMapper.selectById(bedId);
            if (bed != null) {
                bedNumber = bed.getBedNumber();
                Room room = roomMapper.selectById(bed.getRoomId());
                if (room != null) {
                    roomNumber = room.getRoomNumber();
                }
            }
        }

        return ResidentDetailVO.builder()
                .id(resident.getId())
                .name(resident.getName())
                .gender(resident.getGender())
                .age(resident.getAge())
                .birthday(resident.getBirthday())
                .idCard(resident.getIdCard())
                .phone(resident.getPhone())
                .status(resident.getStatus())
                .admissionDate(resident.getAdmissionDate())
                .dischargeDate(resident.getDischargeDate())
                .emergencyName(resident.getEmergencyName())
                .emergencyPhone(resident.getEmergencyPhone())
                .emergencyRelation(resident.getEmergencyRelation())
                .medicalHistory(resident.getMedicalHistory())
                .allergies(resident.getAllergies())
                .remark(resident.getRemark())
                .careLevelName(careLevelName)
                .careLevelId(careLevelId)
                .roomNumber(roomNumber)
                .bedNumber(bedNumber)
                .bedId(bedId)
                .createdAt(resident.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public Long createResident(ResidentSaveRequest request) {
        Resident resident = toEntity(request);
        residentMapper.insert(resident);
        log.info("新增老人成功: id={}, name={}", resident.getId(), resident.getName());
        return resident.getId();
    }

    @Override
    @Transactional
    public void updateResident(Long id, ResidentSaveRequest request) {
        Resident existing = residentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }
        Resident resident = toEntity(request);
        resident.setId(id);
        residentMapper.updateById(resident);
        log.info("更新老人成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteResident(Long id) {
        Resident existing = residentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }

        // 级联清理关联数据，避免孤立记录导致页面显示空名
        checkInRecordMapper.delete(new LambdaQueryWrapper<CheckInRecord>().eq(CheckInRecord::getResidentId, id));
        residentCareLevelMapper.delete(new LambdaQueryWrapper<ResidentCareLevel>().eq(ResidentCareLevel::getResidentId, id));
        healthRecordMapper.delete(new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, id));
        mealRecordMapper.delete(new LambdaQueryWrapper<MealRecord>().eq(MealRecord::getResidentId, id));
        dietaryRestrictionMapper.delete(new LambdaQueryWrapper<DietaryRestriction>().eq(DietaryRestriction::getResidentId, id));

        residentMapper.deleteById(id);
        log.info("删除老人及关联数据成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareLevelVO> listCareLevels() {
        List<CareLevel> levels = careLevelMapper.selectList(
                new LambdaQueryWrapper<CareLevel>().orderByAsc(CareLevel::getSortOrder));
        return levels.stream().map(cl -> CareLevelVO.builder()
                .id(cl.getId())
                .levelName(cl.getLevelName())
                .levelCode(cl.getLevelCode())
                .description(cl.getDescription())
                .dailyFee(cl.getDailyFee())
                .sortOrder(cl.getSortOrder())
                .build()).toList();
    }

    /**
     * 变更护理级别 — 不直接更新旧记录，而是旧记录失效 + 新增生效记录，保留完整的历史变更轨迹。
     */
    @Override
    @Transactional
    public void changeCareLevel(Long residentId, Long careLevelId) {
        if (residentMapper.selectById(residentId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }
        if (careLevelMapper.selectById(careLevelId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "护理级别不存在");
        }

        // 将当前生效的护理级别标记为失效
        ResidentCareLevel current = residentCareLevelMapper.selectCurrentByResidentId(residentId);
        if (current != null) {
            current.setStatus(CareLevelStatusEnum.INACTIVE.getCode());
            current.setExpireDate(LocalDate.now());
            residentCareLevelMapper.updateById(current);
        }

        // 插入一条新的生效记录
        ResidentCareLevel newLevel = new ResidentCareLevel();
        newLevel.setResidentId(residentId);
        newLevel.setCareLevelId(careLevelId);
        newLevel.setEffectiveDate(LocalDate.now());
        newLevel.setStatus(CareLevelStatusEnum.ACTIVE.getCode());
        residentCareLevelMapper.insert(newLevel);

        log.info("变更老人护理级别成功: residentId={}, careLevelId={}", residentId, careLevelId);
    }

    private Resident toEntity(ResidentSaveRequest request) {
        Resident resident = new Resident();
        resident.setName(request.getName());
        resident.setGender(request.getGender());
        resident.setAge(request.getAge());
        resident.setBirthday(request.getBirthday());
        resident.setIdCard(request.getIdCard());
        resident.setPhone(request.getPhone());
        resident.setEmergencyName(request.getEmergencyName());
        resident.setEmergencyPhone(request.getEmergencyPhone());
        resident.setEmergencyRelation(request.getEmergencyRelation());
        resident.setAdmissionDate(request.getAdmissionDate());
        resident.setMedicalHistory(request.getMedicalHistory());
        resident.setAllergies(request.getAllergies());
        resident.setRemark(request.getRemark());
        return resident;
    }

    /**
     * 批量构建 residentId → careLevelName 映射。
     * 两层查找：先查中间表 resident_care_level 得到 careLevelId，再批量查 care_level 表获取名称。
     */
    private Map<Long, String> buildCareLevelNameMap(List<Long> residentIds) {
        Map<Long, String> result = new HashMap<>();
        List<ResidentCareLevel> careLevels = residentCareLevelMapper.selectList(
                new LambdaQueryWrapper<ResidentCareLevel>()
                        .in(ResidentCareLevel::getResidentId, residentIds)
                        .eq(ResidentCareLevel::getStatus, CareLevelStatusEnum.ACTIVE.getCode()));
        if (CollUtil.isNotEmpty(careLevels)) {
            Set<Long> clIds = careLevels.stream()
                    .map(ResidentCareLevel::getCareLevelId).collect(Collectors.toSet());
            Map<Long, String> clNameMap = careLevelMapper.selectBatchIds(clIds).stream()
                    .collect(Collectors.toMap(CareLevel::getId, CareLevel::getLevelName));
            for (ResidentCareLevel rcl : careLevels) {
                String name = clNameMap.get(rcl.getCareLevelId());
                if (name != null) {
                    result.put(rcl.getResidentId(), name);
                }
            }
        }
        return result;
    }

    /**
     * 批量构建 residentId → {roomNumber, bedNumber} 映射。
     * 三层查找：check_in_record → bed → room，皆用批量查询，避免 N+1。
     */
    private void buildRoomBedMaps(List<Long> residentIds,
                                   Map<Long, String> roomNumberMap,
                                   Map<Long, String> bedNumberMap) {
        List<CheckInRecord> checkIns = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .in(CheckInRecord::getResidentId, residentIds)
                        .eq(CheckInRecord::getStatus, CheckInStatusEnum.CHECKED_IN.getCode()));
        if (CollUtil.isEmpty(checkIns)) {
            return;
        }

        Set<Long> bedIds = checkIns.stream()
                .map(CheckInRecord::getBedId).collect(Collectors.toSet());
        Map<Long, Bed> bedMap = bedMapper.selectBatchIds(bedIds).stream()
                .collect(Collectors.toMap(Bed::getId, Function.identity()));

        Set<Long> roomIds = bedMap.values().stream()
                .map(Bed::getRoomId).collect(Collectors.toSet());
        Map<Long, Room> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));

        for (CheckInRecord ci : checkIns) {
            Bed bed = bedMap.get(ci.getBedId());
            if (bed != null) {
                bedNumberMap.put(ci.getResidentId(), bed.getBedNumber());
                Room room = roomMap.get(bed.getRoomId());
                if (room != null) {
                    roomNumberMap.put(ci.getResidentId(), room.getRoomNumber());
                }
            }
        }
    }
}
