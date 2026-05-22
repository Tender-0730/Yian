package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.*;
import com.yian.entity.*;
import com.yian.enums.HandoverStatusEnum;
import com.yian.enums.NurseStatusEnum;
import com.yian.enums.ScheduleStatusEnum;
import com.yian.mapper.*;
import com.yian.service.NurseService;
import com.yian.vo.NurseInfoVO;
import com.yian.vo.NurseScheduleVO;
import com.yian.vo.ShiftHandoverVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NurseServiceImpl implements NurseService {

    private final NurseInfoMapper nurseInfoMapper;
    private final NurseScheduleMapper nurseScheduleMapper;
    private final ShiftHandoverMapper shiftHandoverMapper;
    private final SysUserMapper sysUserMapper;

    // ==================== 护工信息 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<NurseInfoVO> pageNurses(NurseQuery query) {
        LambdaQueryWrapper<NurseInfo> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(NurseInfo::getStatus, query.getStatus());
        }
        if (query.getShiftPreference() != null) {
            wrapper.eq(NurseInfo::getShiftPreference, query.getShiftPreference());
        }
        wrapper.orderByDesc(NurseInfo::getCreatedAt);

        Page<NurseInfo> page = new Page<>(query.getPage(), query.getSize());
        Page<NurseInfo> result = nurseInfoMapper.selectPage(page, wrapper);
        List<NurseInfo> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Map<Long, String> userNameMap = buildUserNameMap(records.stream()
                .map(NurseInfo::getUserId).collect(Collectors.toSet()));

        List<NurseInfoVO> vos = records.stream().map(r -> NurseInfoVO.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .nurseName(userNameMap.get(r.getUserId()))
                .employeeNo(r.getEmployeeNo())
                .phone(r.getPhone())
                .shiftPreference(r.getShiftPreference())
                .status(r.getStatus())
                .remark(r.getRemark())
                .createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public NurseInfoVO getNurseById(Long id) {
        NurseInfo r = nurseInfoMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "护工不存在");
        }
        SysUser user = sysUserMapper.selectById(r.getUserId());
        return NurseInfoVO.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .nurseName(user != null ? user.getRealName() : null)
                .employeeNo(r.getEmployeeNo())
                .phone(r.getPhone())
                .shiftPreference(r.getShiftPreference())
                .status(r.getStatus())
                .remark(r.getRemark())
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public Long createNurse(NurseInfoSaveRequest request) {
        if (sysUserMapper.selectById(request.getUserId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "系统用户不存在");
        }
        NurseInfo r = new NurseInfo();
        r.setUserId(request.getUserId());
        r.setEmployeeNo(request.getEmployeeNo());
        r.setPhone(request.getPhone());
        r.setShiftPreference(request.getShiftPreference());
        r.setStatus(NurseStatusEnum.ACTIVE.getCode());
        r.setRemark(request.getRemark());
        nurseInfoMapper.insert(r);
        log.info("新增护工成功: id={}, userId={}", r.getId(), r.getUserId());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateNurse(Long id, NurseInfoSaveRequest request) {
        NurseInfo existing = nurseInfoMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "护工不存在");
        }
        NurseInfo r = new NurseInfo();
        r.setId(id);
        r.setUserId(request.getUserId());
        r.setEmployeeNo(request.getEmployeeNo());
        r.setPhone(request.getPhone());
        r.setShiftPreference(request.getShiftPreference());
        r.setRemark(request.getRemark());
        nurseInfoMapper.updateById(r);
        log.info("更新护工信息成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteNurse(Long id) {
        if (nurseInfoMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "护工不存在");
        }
        // 级联删除排班记录
        nurseScheduleMapper.delete(new LambdaQueryWrapper<NurseSchedule>().eq(NurseSchedule::getNurseId, id));
        // 级联删除交接记录（from_nurse 或 to_nurse 关联）
        shiftHandoverMapper.delete(new LambdaQueryWrapper<ShiftHandover>()
                .eq(ShiftHandover::getFromNurseId, id)
                .or()
                .eq(ShiftHandover::getToNurseId, id));
        nurseInfoMapper.deleteById(id);
        log.info("删除护工及关联数据成功: id={}", id);
    }

    // ==================== 排班 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<NurseScheduleVO> pageSchedules(NurseQuery query) {
        LambdaQueryWrapper<NurseSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(NurseSchedule::getShiftDate)
                .orderByAsc(NurseSchedule::getShiftType);

        Page<NurseSchedule> page = new Page<>(query.getPage(), query.getSize());
        Page<NurseSchedule> result = nurseScheduleMapper.selectPage(page, wrapper);
        List<NurseSchedule> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Map<Long, String> nurseNameMap = buildNurseNameMap(records.stream()
                .map(NurseSchedule::getNurseId).collect(Collectors.toSet()));

        List<NurseScheduleVO> vos = records.stream().map(r -> NurseScheduleVO.builder()
                .id(r.getId())
                .nurseId(r.getNurseId())
                .nurseName(nurseNameMap.get(r.getNurseId()))
                .shiftDate(r.getShiftDate())
                .shiftType(r.getShiftType())
                .status(r.getStatus())
                .notes(r.getNotes())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createSchedule(NurseScheduleSaveRequest request) {
        if (nurseInfoMapper.selectById(request.getNurseId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "护工不存在");
        }
        NurseSchedule r = new NurseSchedule();
        r.setNurseId(request.getNurseId());
        r.setShiftDate(request.getShiftDate());
        r.setShiftType(request.getShiftType());
        r.setStatus(ScheduleStatusEnum.SCHEDULED.getCode());
        r.setNotes(request.getNotes());
        try {
            nurseScheduleMapper.insert(r);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该护工当天此班次已有排班");
        }
        log.info("新增排班成功: id={}, nurseId={}", r.getId(), r.getNurseId());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateSchedule(Long id, NurseScheduleSaveRequest request) {
        NurseSchedule existing = nurseScheduleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "排班记录不存在");
        }
        NurseSchedule r = new NurseSchedule();
        r.setId(id);
        r.setNurseId(request.getNurseId());
        r.setShiftDate(request.getShiftDate());
        r.setShiftType(request.getShiftType());
        r.setStatus(request.getStatus());
        r.setNotes(request.getNotes());
        try {
            nurseScheduleMapper.updateById(r);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该护工当天此班次已有排班");
        }
        log.info("更新排班成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (nurseScheduleMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "排班记录不存在");
        }
        nurseScheduleMapper.deleteById(id);
        log.info("删除排班成功: id={}", id);
    }

    // ==================== 交接班 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<ShiftHandoverVO> pageHandovers(NurseQuery query) {
        LambdaQueryWrapper<ShiftHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ShiftHandover::getHandoverTime);

        Page<ShiftHandover> page = new Page<>(query.getPage(), query.getSize());
        Page<ShiftHandover> result = shiftHandoverMapper.selectPage(page, wrapper);
        List<ShiftHandover> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Set<Long> nurseIds = records.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getFromNurseId(), r.getToNurseId()))
                .collect(Collectors.toSet());
        Map<Long, String> nurseNameMap = buildNurseNameMap(nurseIds);

        List<ShiftHandoverVO> vos = records.stream().map(r -> ShiftHandoverVO.builder()
                .id(r.getId())
                .scheduleId(r.getScheduleId())
                .shiftDate(r.getShiftDate())
                .fromNurseId(r.getFromNurseId())
                .fromNurseName(nurseNameMap.get(r.getFromNurseId()))
                .toNurseId(r.getToNurseId())
                .toNurseName(nurseNameMap.get(r.getToNurseId()))
                .handoverTime(r.getHandoverTime())
                .keyNotes(r.getKeyNotes())
                .residentNotes(r.getResidentNotes())
                .materialCheck(r.getMaterialCheck())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createHandover(ShiftHandoverSaveRequest request) {
        NurseSchedule schedule = nurseScheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "排班记录不存在");
        }
        if (nurseInfoMapper.selectById(request.getToNurseId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接班护工不存在");
        }

        ShiftHandover h = new ShiftHandover();
        h.setScheduleId(request.getScheduleId());
        h.setShiftDate(request.getShiftDate() != null ? request.getShiftDate() : schedule.getShiftDate());
        h.setFromNurseId(schedule.getNurseId());
        h.setToNurseId(request.getToNurseId());
        h.setHandoverTime(LocalDateTime.now());
        h.setKeyNotes(request.getKeyNotes());
        h.setResidentNotes(request.getResidentNotes());
        h.setMaterialCheck(request.getMaterialCheck());
        h.setStatus(HandoverStatusEnum.PENDING.getCode());
        shiftHandoverMapper.insert(h);

        // 同步更新排班状态为已完成
        schedule.setStatus(ScheduleStatusEnum.COMPLETED.getCode());
        nurseScheduleMapper.updateById(schedule);

        log.info("创建交接记录成功: id={}, fromNurseId={}, toNurseId={}", h.getId(), schedule.getNurseId(), request.getToNurseId());
        return h.getId();
    }

    // ==================== 工具方法 ====================

    private Map<Long, String> buildUserNameMap(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) return Map.of();
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
    }

    private Map<Long, String> buildNurseNameMap(Set<Long> nurseIds) {
        if (CollUtil.isEmpty(nurseIds)) return Map.of();
        List<NurseInfo> nurses = nurseInfoMapper.selectBatchIds(nurseIds);
        Set<Long> userIds = nurses.stream().map(NurseInfo::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = buildUserNameMap(userIds);
        return nurses.stream()
                .collect(Collectors.toMap(NurseInfo::getId,
                        n -> userNameMap.getOrDefault(n.getUserId(), null)));
    }
}
