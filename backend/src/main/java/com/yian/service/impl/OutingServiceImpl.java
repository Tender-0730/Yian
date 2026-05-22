package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.OutingQuery;
import com.yian.dto.OutingSaveRequest;
import com.yian.entity.OutingRecord;
import com.yian.entity.Resident;
import com.yian.enums.OutingStatusEnum;
import com.yian.mapper.OutingRecordMapper;
import com.yian.mapper.ResidentMapper;
import com.yian.service.OutingService;
import com.yian.vo.OutingRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutingServiceImpl implements OutingService {

    private final OutingRecordMapper outingRecordMapper;
    private final ResidentMapper residentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<OutingRecordVO> pageOutings(OutingQuery query) {
        LambdaQueryWrapper<OutingRecord> wrapper = new LambdaQueryWrapper<>();
        if (query.getResidentId() != null) {
            wrapper.eq(OutingRecord::getResidentId, query.getResidentId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OutingRecord::getStatus, query.getStatus());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(OutingRecord::getOutTime, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(OutingRecord::getOutTime, query.getEndDate());
        }
        wrapper.orderByDesc(OutingRecord::getOutTime);

        Page<OutingRecord> page = new Page<>(query.getPage(), query.getSize());
        Page<OutingRecord> result = outingRecordMapper.selectPage(page, wrapper);
        List<OutingRecord> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Map<Long, String> nameMap = buildResidentNameMap(records);
        LocalDateTime now = LocalDateTime.now();

        List<OutingRecordVO> vos = records.stream().map(r -> {
            boolean overdue = OutingStatusEnum.OUT.getCode().equals(r.getStatus())
                    && r.getExpectedReturnTime() != null
                    && r.getExpectedReturnTime().isBefore(now);
            return OutingRecordVO.builder()
                    .id(r.getId())
                    .residentId(r.getResidentId())
                    .residentName(nameMap.get(r.getResidentId()))
                    .outTime(r.getOutTime())
                    .expectedReturnTime(r.getExpectedReturnTime())
                    .actualReturnTime(r.getActualReturnTime())
                    .destination(r.getDestination())
                    .companion(r.getCompanion())
                    .reason(r.getReason())
                    .status(r.getStatus())
                    .notes(r.getNotes())
                    .registeredBy(r.getRegisteredBy())
                    .createdAt(r.getCreatedAt())
                    .isOverdue(overdue)
                    .overdueMinutes(overdue ? Duration.between(r.getExpectedReturnTime(), now).toMinutes() : null)
                    .build();
        }).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public OutingRecordVO getOutingById(Long id) {
        OutingRecord r = outingRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "外出记录不存在");
        }
        String residentName = residentMapper.selectNameById(r.getResidentId());
        LocalDateTime now = LocalDateTime.now();
        boolean overdue = OutingStatusEnum.OUT.getCode().equals(r.getStatus())
                && r.getExpectedReturnTime() != null
                && r.getExpectedReturnTime().isBefore(now);
        return OutingRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(residentName)
                .outTime(r.getOutTime())
                .expectedReturnTime(r.getExpectedReturnTime())
                .actualReturnTime(r.getActualReturnTime())
                .destination(r.getDestination())
                .companion(r.getCompanion())
                .reason(r.getReason())
                .status(r.getStatus())
                .notes(r.getNotes())
                .registeredBy(r.getRegisteredBy())
                .createdAt(r.getCreatedAt())
                .isOverdue(overdue)
                .overdueMinutes(overdue ? Duration.between(r.getExpectedReturnTime(), now).toMinutes() : null)
                .build();
    }

    @Override
    @Transactional
    public Long createOuting(OutingSaveRequest request) {
        if (residentMapper.selectById(request.getResidentId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }
        OutingRecord r = new OutingRecord();
        r.setResidentId(request.getResidentId());
        r.setOutTime(request.getOutTime());
        r.setExpectedReturnTime(request.getExpectedReturnTime());
        r.setDestination(request.getDestination());
        r.setCompanion(request.getCompanion());
        r.setReason(request.getReason());
        r.setNotes(request.getNotes());
        r.setStatus(OutingStatusEnum.OUT.getCode());
        outingRecordMapper.insert(r);
        log.info("新增外出登记成功: id={}, residentId={}", r.getId(), r.getResidentId());
        return r.getId();
    }

    @Override
    @Transactional
    public void returnOuting(Long id) {
        OutingRecord r = outingRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "外出记录不存在");
        }
        if (!OutingStatusEnum.OUT.getCode().equals(r.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该记录已返回，不可重复操作");
        }
        r.setActualReturnTime(LocalDateTime.now());
        r.setStatus(OutingStatusEnum.RETURNED.getCode());
        outingRecordMapper.updateById(r);
        log.info("登记返回成功: id={}", id);
    }

    @Override
    @Transactional
    public void cancelOuting(Long id) {
        OutingRecord r = outingRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "外出记录不存在");
        }
        if (!OutingStatusEnum.OUT.getCode().equals(r.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已返回的记录不可取消");
        }
        outingRecordMapper.deleteById(id);
        log.info("取消外出登记成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutingRecordVO> listOverdue() {
        List<OutingRecord> records = outingRecordMapper.selectList(
                new LambdaQueryWrapper<OutingRecord>()
                        .eq(OutingRecord::getStatus, OutingStatusEnum.OUT.getCode())
                        .lt(OutingRecord::getExpectedReturnTime, LocalDateTime.now())
                        .orderByAsc(OutingRecord::getExpectedReturnTime));
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        Map<Long, String> nameMap = buildResidentNameMap(records);
        LocalDateTime now = LocalDateTime.now();
        return records.stream().map(r -> OutingRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(nameMap.get(r.getResidentId()))
                .outTime(r.getOutTime())
                .expectedReturnTime(r.getExpectedReturnTime())
                .actualReturnTime(r.getActualReturnTime())
                .destination(r.getDestination())
                .companion(r.getCompanion())
                .reason(r.getReason())
                .status(r.getStatus())
                .notes(r.getNotes())
                .registeredBy(r.getRegisteredBy())
                .createdAt(r.getCreatedAt())
                .isOverdue(true)
                .overdueMinutes(Duration.between(r.getExpectedReturnTime(), now).toMinutes())
                .build()).toList();
    }

    private Map<Long, String> buildResidentNameMap(List<OutingRecord> records) {
        Set<Long> residentIds = records.stream()
                .map(OutingRecord::getResidentId).collect(Collectors.toSet());
        List<Resident> residents = residentMapper.selectNameByIds(residentIds.stream().toList());
        return residents.stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));
    }
}
