package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.HealthRecordQuery;
import com.yian.dto.HealthRecordSaveRequest;
import com.yian.entity.HealthRecord;
import com.yian.entity.Resident;
import com.yian.mapper.HealthRecordMapper;
import com.yian.mapper.ResidentMapper;
import com.yian.security.LoginUser;
import com.yian.service.HealthRecordService;
import com.yian.vo.HealthRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;
    private final ResidentMapper residentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<HealthRecordVO> pageHealthRecords(HealthRecordQuery query) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        if (query.getResidentId() != null) {
            wrapper.eq(HealthRecord::getResidentId, query.getResidentId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(HealthRecord::getRecordedAt, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(HealthRecord::getRecordedAt, query.getEndDate());
        }
        if (query.getStatus() != null) {
            wrapper.eq(HealthRecord::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(HealthRecord::getRecordedAt);

        Page<HealthRecord> page = new Page<>(query.getPage(), query.getSize());
        Page<HealthRecord> result = healthRecordMapper.selectPage(page, wrapper);
        List<HealthRecord> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Map<Long, String> nameMap = buildResidentNameMap(records);

        List<HealthRecordVO> vos = records.stream().map(r -> HealthRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(nameMap.get(r.getResidentId()))
                .temperature(r.getTemperature())
                .bloodSystolic(r.getBloodSystolic())
                .bloodDiastolic(r.getBloodDiastolic())
                .heartRate(r.getHeartRate())
                .bloodSugar(r.getBloodSugar())
                .weight(r.getWeight())
                .oxygen(r.getOxygen())
                .status(r.getStatus())
                .abnormal(r.getAbnormal())
                .notes(r.getNotes())
                .recordedBy(r.getRecordedBy())
                .recordedAt(r.getRecordedAt())
                .createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordVO getHealthRecordById(Long id) {
        HealthRecord r = healthRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "健康记录不存在");
        }
        String residentName = residentMapper.selectNameById(r.getResidentId());
        return HealthRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(residentName)
                .temperature(r.getTemperature())
                .bloodSystolic(r.getBloodSystolic())
                .bloodDiastolic(r.getBloodDiastolic())
                .heartRate(r.getHeartRate())
                .bloodSugar(r.getBloodSugar())
                .weight(r.getWeight())
                .oxygen(r.getOxygen())
                .status(r.getStatus())
                .abnormal(r.getAbnormal())
                .notes(r.getNotes())
                .recordedBy(r.getRecordedBy())
                .recordedAt(r.getRecordedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public Long createHealthRecord(HealthRecordSaveRequest request) {
        if (residentMapper.selectById(request.getResidentId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }

        HealthRecord r = new HealthRecord();
        r.setResidentId(request.getResidentId());
        r.setTemperature(request.getTemperature());
        r.setBloodSystolic(request.getBloodSystolic());
        r.setBloodDiastolic(request.getBloodDiastolic());
        r.setHeartRate(request.getHeartRate());
        r.setBloodSugar(request.getBloodSugar());
        r.setWeight(request.getWeight());
        r.setOxygen(request.getOxygen());
        r.setStatus(request.getStatus());
        r.setAbnormal(request.getAbnormal());
        r.setNotes(request.getNotes());
        // 未传 recordedAt 时默认取当前时间
        r.setRecordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now());
        r.setRecordedBy(getCurrentUserId());

        healthRecordMapper.insert(r);
        log.info("新增健康记录成功: id={}, residentId={}", r.getId(), r.getResidentId());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateHealthRecord(Long id, HealthRecordSaveRequest request) {
        HealthRecord existing = healthRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "健康记录不存在");
        }

        HealthRecord r = new HealthRecord();
        r.setId(id);
        r.setResidentId(request.getResidentId());
        r.setTemperature(request.getTemperature());
        r.setBloodSystolic(request.getBloodSystolic());
        r.setBloodDiastolic(request.getBloodDiastolic());
        r.setHeartRate(request.getHeartRate());
        r.setBloodSugar(request.getBloodSugar());
        r.setWeight(request.getWeight());
        r.setOxygen(request.getOxygen());
        r.setStatus(request.getStatus());
        r.setAbnormal(request.getAbnormal());
        r.setNotes(request.getNotes());
        r.setRecordedAt(request.getRecordedAt());

        healthRecordMapper.updateById(r);
        log.info("更新健康记录成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteHealthRecord(Long id) {
        if (healthRecordMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "健康记录不存在");
        }
        healthRecordMapper.deleteById(id);
        log.info("删除健康记录成功: id={}", id);
    }

    private static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user.getUserId();
        }
        return null;
    }

    /**
     * 批量构建 residentId → name 映射，用于在 VO 中显示老人姓名，避免逐条调用 selectNameById（N+1）。
     */
    private Map<Long, String> buildResidentNameMap(List<HealthRecord> records) {
        Set<Long> residentIds = records.stream()
                .map(HealthRecord::getResidentId).collect(Collectors.toSet());
        List<Resident> residents = residentMapper.selectNameByIds(
                residentIds.stream().toList());
        return residents.stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));
    }
}
