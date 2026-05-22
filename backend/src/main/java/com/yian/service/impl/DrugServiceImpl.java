package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.*;
import com.yian.entity.*;
import com.yian.enums.DrugInventoryStatusEnum;
import com.yian.enums.DrugPrescriptionStatusEnum;
import com.yian.enums.DrugRecordStatusEnum;
import com.yian.mapper.*;
import com.yian.service.DrugService;
import com.yian.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugDictMapper drugDictMapper;
    private final DrugInventoryMapper drugInventoryMapper;
    private final DrugInventoryLogMapper drugInventoryLogMapper;
    private final DrugPrescriptionMapper drugPrescriptionMapper;
    private final DrugRecordMapper drugRecordMapper;
    private final ResidentMapper residentMapper;

    // ==================== 药品字典 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<DrugDictVO> pageDrugDicts(DrugDictQuery query) {
        LambdaQueryWrapper<DrugDict> wrapper = new LambdaQueryWrapper<>();
        if (query.getDrugName() != null) {
            wrapper.like(DrugDict::getDrugName, query.getDrugName());
        }
        if (query.getCategory() != null) {
            wrapper.eq(DrugDict::getCategory, query.getCategory());
        }
        wrapper.orderByDesc(DrugDict::getCreatedAt);

        Page<DrugDict> page = new Page<>(query.getPage(), query.getSize());
        Page<DrugDict> result = drugDictMapper.selectPage(page, wrapper);
        List<DrugDict> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        List<DrugDictVO> vos = records.stream().map(r -> DrugDictVO.builder()
                .id(r.getId()).drugName(r.getDrugName()).specification(r.getSpecification())
                .manufacturer(r.getManufacturer()).unit(r.getUnit()).category(r.getCategory())
                .description(r.getDescription()).status(r.getStatus()).createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public DrugDictVO getDrugDictById(Long id) {
        DrugDict r = drugDictMapper.selectById(id);
        if (r == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "药品不存在");
        return DrugDictVO.builder()
                .id(r.getId()).drugName(r.getDrugName()).specification(r.getSpecification())
                .manufacturer(r.getManufacturer()).unit(r.getUnit()).category(r.getCategory())
                .description(r.getDescription()).status(r.getStatus()).createdAt(r.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public Long createDrugDict(DrugDictSaveRequest request) {
        DrugDict r = new DrugDict();
        r.setDrugName(request.getDrugName());
        r.setSpecification(request.getSpecification());
        r.setManufacturer(request.getManufacturer());
        r.setUnit(request.getUnit());
        r.setCategory(request.getCategory());
        r.setDescription(request.getDescription());
        r.setStatus("ACTIVE");
        drugDictMapper.insert(r);
        log.info("新增药品字典成功: id={}, name={}", r.getId(), r.getDrugName());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateDrugDict(Long id, DrugDictSaveRequest request) {
        DrugDict existing = drugDictMapper.selectById(id);
        if (existing == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "药品不存在");
        DrugDict r = new DrugDict();
        r.setId(id);
        r.setDrugName(request.getDrugName());
        r.setSpecification(request.getSpecification());
        r.setManufacturer(request.getManufacturer());
        r.setUnit(request.getUnit());
        r.setCategory(request.getCategory());
        r.setDescription(request.getDescription());
        drugDictMapper.updateById(r);
        log.info("更新药品字典成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteDrugDict(Long id) {
        if (drugDictMapper.selectById(id) == null)
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "药品不存在");
        drugDictMapper.deleteById(id);
        log.info("删除药品字典成功: id={}", id);
    }

    // ==================== 库存 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<DrugInventoryVO> pageInventories(DrugDictQuery query) {
        LambdaQueryWrapper<DrugInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DrugInventory::getCreatedAt);

        Page<DrugInventory> page = new Page<>(query.getPage(), query.getSize());
        Page<DrugInventory> result = drugInventoryMapper.selectPage(page, wrapper);
        List<DrugInventory> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Set<Long> drugIds = records.stream().map(DrugInventory::getDrugId).collect(Collectors.toSet());
        Map<Long, String> drugNameMap = drugDictMapper.selectBatchIds(drugIds).stream()
                .collect(Collectors.toMap(DrugDict::getId, DrugDict::getDrugName));
        LocalDate today = LocalDate.now();

        List<DrugInventoryVO> vos = records.stream().map(r -> DrugInventoryVO.builder()
                .id(r.getId()).drugId(r.getDrugId()).drugName(drugNameMap.get(r.getDrugId()))
                .quantity(r.getQuantity()).alertThreshold(r.getAlertThreshold())
                .location(r.getLocation()).batchNo(r.getBatchNo()).expireDate(r.getExpireDate())
                .status(r.getStatus())
                .isExpired(r.getExpireDate() != null && r.getExpireDate().isBefore(today))
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public void inbound(Long id, int quantity, String reason) {
        if (quantity <= 0) throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "入库数量必须大于0");
        changeQuantity(id, quantity, "INBOUND", reason);
    }

    @Override
    @Transactional
    public void outbound(Long id, int quantity, String reason) {
        if (quantity <= 0) throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "出库数量必须大于0");
        DrugInventory inv = drugInventoryMapper.selectById(id);
        if (inv == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "库存记录不存在");
        if (inv.getQuantity() < quantity)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足，当前库存: " + inv.getQuantity());
        changeQuantity(id, -quantity, "OUTBOUND", reason);
    }

    private void changeQuantity(Long inventoryId, int delta, String changeType, String reason) {
        DrugInventory inv = drugInventoryMapper.selectById(inventoryId);
        int before = inv.getQuantity();
        int after = before + delta;
        if (after < 0) after = 0;

        inv.setQuantity(after);
        if (after <= 0) {
            inv.setStatus(DrugInventoryStatusEnum.DEPLETED.getCode());
        } else if (inv.getAlertThreshold() != null && after <= inv.getAlertThreshold()) {
            inv.setStatus(DrugInventoryStatusEnum.LOW_STOCK.getCode());
        } else {
            inv.setStatus(DrugInventoryStatusEnum.ACTIVE.getCode());
        }
        drugInventoryMapper.updateById(inv);

        DrugInventoryLog logEntry = new DrugInventoryLog();
        logEntry.setInventoryId(inventoryId);
        logEntry.setDrugId(inv.getDrugId());
        logEntry.setChangeType(changeType);
        logEntry.setChangeQuantity(delta);
        logEntry.setBeforeQuantity(before);
        logEntry.setAfterQuantity(after);
        logEntry.setReason(reason);
        drugInventoryLogMapper.insert(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DrugInventoryVO> listAlerts() {
        List<DrugInventory> records = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .in(DrugInventory::getStatus, DrugInventoryStatusEnum.LOW_STOCK.getCode(),
                                DrugInventoryStatusEnum.DEPLETED.getCode())
                        .orderByAsc(DrugInventory::getStatus));
        if (CollUtil.isEmpty(records)) return List.of();

        Set<Long> drugIds = records.stream().map(DrugInventory::getDrugId).collect(Collectors.toSet());
        Map<Long, String> drugNameMap = drugDictMapper.selectBatchIds(drugIds).stream()
                .collect(Collectors.toMap(DrugDict::getId, DrugDict::getDrugName));
        LocalDate today = LocalDate.now();

        return records.stream().map(r -> DrugInventoryVO.builder()
                .id(r.getId()).drugId(r.getDrugId()).drugName(drugNameMap.get(r.getDrugId()))
                .quantity(r.getQuantity()).alertThreshold(r.getAlertThreshold())
                .location(r.getLocation()).batchNo(r.getBatchNo()).expireDate(r.getExpireDate())
                .status(r.getStatus())
                .isExpired(r.getExpireDate() != null && r.getExpireDate().isBefore(today))
                .build()).toList();
    }

    // ==================== 处方 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<DrugPrescriptionVO> pagePrescriptions(DrugDictQuery query) {
        LambdaQueryWrapper<DrugPrescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DrugPrescription::getCreatedAt);

        Page<DrugPrescription> page = new Page<>(query.getPage(), query.getSize());
        Page<DrugPrescription> result = drugPrescriptionMapper.selectPage(page, wrapper);
        List<DrugPrescription> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Set<Long> residentIds = records.stream().map(DrugPrescription::getResidentId).collect(Collectors.toSet());
        Set<Long> drugIds = records.stream().map(DrugPrescription::getDrugId).collect(Collectors.toSet());
        Map<Long, String> residentNameMap = residentMapper.selectNameByIds(new ArrayList<>(residentIds)).stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));
        Map<Long, String> drugNameMap = drugDictMapper.selectBatchIds(drugIds).stream()
                .collect(Collectors.toMap(DrugDict::getId, DrugDict::getDrugName));

        List<DrugPrescriptionVO> vos = records.stream().map(r -> DrugPrescriptionVO.builder()
                .id(r.getId()).residentId(r.getResidentId()).residentName(residentNameMap.get(r.getResidentId()))
                .drugId(r.getDrugId()).drugName(drugNameMap.get(r.getDrugId()))
                .dosage(r.getDosage()).frequency(r.getFrequency()).dosesPerDay(r.getDosesPerDay())
                .startDate(r.getStartDate()).endDate(r.getEndDate()).status(r.getStatus())
                .prescribedBy(r.getPrescribedBy()).notes(r.getNotes())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null)
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createPrescription(DrugPrescriptionSaveRequest request) {
        if (residentMapper.selectById(request.getResidentId()) == null)
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        if (drugDictMapper.selectById(request.getDrugId()) == null)
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "药品不存在");

        DrugPrescription r = new DrugPrescription();
        r.setResidentId(request.getResidentId());
        r.setDrugId(request.getDrugId());
        r.setDosage(request.getDosage());
        r.setFrequency(request.getFrequency());
        r.setDosesPerDay(request.getDosesPerDay());
        r.setStartDate(request.getStartDate());
        r.setEndDate(request.getEndDate());
        r.setStatus(DrugPrescriptionStatusEnum.ACTIVE.getCode());
        r.setPrescribedBy(request.getPrescribedBy());
        r.setNotes(request.getNotes());
        drugPrescriptionMapper.insert(r);
        log.info("新增处方成功: id={}, residentId={}", r.getId(), r.getResidentId());
        return r.getId();
    }

    @Override
    @Transactional
    public void updatePrescription(Long id, DrugPrescriptionSaveRequest request) {
        DrugPrescription existing = drugPrescriptionMapper.selectById(id);
        if (existing == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "处方不存在");
        DrugPrescription r = new DrugPrescription();
        r.setId(id);
        r.setResidentId(request.getResidentId());
        r.setDrugId(request.getDrugId());
        r.setDosage(request.getDosage());
        r.setFrequency(request.getFrequency());
        r.setDosesPerDay(request.getDosesPerDay());
        r.setStartDate(request.getStartDate());
        r.setEndDate(request.getEndDate());
        r.setStatus(request.getStatus());
        r.setNotes(request.getNotes());
        drugPrescriptionMapper.updateById(r);
        log.info("更新处方成功: id={}", id);
    }

    // ==================== 服药记录 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<DrugRecordVO> pageDrugRecords(DrugDictQuery query) {
        LambdaQueryWrapper<DrugRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DrugRecord::getAdministeredAt);

        Page<DrugRecord> page = new Page<>(query.getPage(), query.getSize());
        Page<DrugRecord> result = drugRecordMapper.selectPage(page, wrapper);
        List<DrugRecord> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Set<Long> residentIds = records.stream().map(DrugRecord::getResidentId).collect(Collectors.toSet());
        Map<Long, String> residentNameMap = residentMapper.selectNameByIds(new ArrayList<>(residentIds)).stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));

        List<DrugRecordVO> vos = records.stream().map(r -> DrugRecordVO.builder()
                .id(r.getId()).prescriptionId(r.getPrescriptionId())
                .residentId(r.getResidentId()).residentName(residentNameMap.get(r.getResidentId()))
                .drugId(r.getDrugId()).drugName(r.getDrugName())
                .administeredAt(r.getAdministeredAt()).administeredBy(r.getAdministeredBy())
                .status(r.getStatus()).notes(r.getNotes()).createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public void createDrugRecord(DrugRecordSaveRequest request) {
        DrugPrescription prescription = drugPrescriptionMapper.selectById(request.getPrescriptionId());
        if (prescription == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "处方不存在");

        DrugDict drug = drugDictMapper.selectById(prescription.getDrugId());

        DrugRecord r = new DrugRecord();
        r.setPrescriptionId(request.getPrescriptionId());
        r.setResidentId(prescription.getResidentId());
        r.setDrugId(prescription.getDrugId());
        r.setDrugName(drug != null ? drug.getDrugName() : null);
        r.setAdministeredAt(LocalDateTime.now());
        r.setStatus(request.getStatus() != null ? request.getStatus() : DrugRecordStatusEnum.TAKEN.getCode());
        r.setNotes(request.getNotes());
        drugRecordMapper.insert(r);

        // TAKEN 状态联动扣减库存
        if (DrugRecordStatusEnum.TAKEN.getCode().equals(r.getStatus())) {
            List<DrugInventory> inventories = drugInventoryMapper.selectList(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, prescription.getDrugId())
                            .gt(DrugInventory::getQuantity, 0)
                            .orderByAsc(DrugInventory::getExpireDate));
            if (CollUtil.isNotEmpty(inventories)) {
                DrugInventory inv = inventories.get(0);
                changeQuantity(inv.getId(), -1, "OUTBOUND", "服药扣减: prescriptionId=" + prescription.getId());
            }
        }

        log.info("新增服药记录成功: id={}, prescriptionId={}", r.getId(), r.getPrescriptionId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingDrugVO> listPending(Long residentId, LocalDate date) {
        if (date == null) date = LocalDate.now();
        final LocalDate effectiveDate = date;

        LambdaQueryWrapper<DrugPrescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugPrescription::getStatus, DrugPrescriptionStatusEnum.ACTIVE.getCode());
        if (residentId != null) {
            wrapper.eq(DrugPrescription::getResidentId, residentId);
        }
        // 处方有效期内
        wrapper.le(DrugPrescription::getStartDate, date);
        wrapper.and(w -> w.isNull(DrugPrescription::getEndDate).or().ge(DrugPrescription::getEndDate, effectiveDate));

        List<DrugPrescription> prescriptions = drugPrescriptionMapper.selectList(wrapper);
        if (CollUtil.isEmpty(prescriptions)) return List.of();

        Set<Long> pIds = prescriptions.stream().map(DrugPrescription::getId).collect(Collectors.toSet());
        Set<Long> residentIds = prescriptions.stream().map(DrugPrescription::getResidentId).collect(Collectors.toSet());
        Set<Long> drugIds = prescriptions.stream().map(DrugPrescription::getDrugId).collect(Collectors.toSet());

        Map<Long, String> residentNameMap = residentMapper.selectNameByIds(new ArrayList<>(residentIds)).stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));
        Map<Long, DrugDict> drugMap = drugDictMapper.selectBatchIds(drugIds).stream()
                .collect(Collectors.toMap(DrugDict::getId, d -> d));

        // 查询今日已服药记录
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<DrugRecord> todayRecords = drugRecordMapper.selectList(
                new LambdaQueryWrapper<DrugRecord>()
                        .in(DrugRecord::getPrescriptionId, pIds)
                        .eq(DrugRecord::getStatus, DrugRecordStatusEnum.TAKEN.getCode())
                        .ge(DrugRecord::getAdministeredAt, dayStart)
                        .lt(DrugRecord::getAdministeredAt, dayEnd));
        Map<Long, Long> takenCountMap = todayRecords.stream()
                .collect(Collectors.groupingBy(DrugRecord::getPrescriptionId, Collectors.counting()));

        // 取每条处方最近一次服药时间
        Map<Long, LocalDateTime> lastTakenMap = todayRecords.stream()
                .collect(Collectors.toMap(DrugRecord::getPrescriptionId,
                        DrugRecord::getAdministeredAt, (a, b) -> a.isAfter(b) ? a : b));

        return prescriptions.stream().map(p -> {
            int dosesPerDay = p.getDosesPerDay() != null ? p.getDosesPerDay() : 1;
            long taken = takenCountMap.getOrDefault(p.getId(), 0L);
            int pending = (int) Math.max(0, dosesPerDay - taken);
            DrugDict drug = drugMap.get(p.getDrugId());
            return PendingDrugVO.builder()
                    .prescriptionId(p.getId())
                    .residentId(p.getResidentId())
                    .residentName(residentNameMap.get(p.getResidentId()))
                    .drugId(p.getDrugId())
                    .drugName(drug != null ? drug.getDrugName() : null)
                    .dosage(p.getDosage())
                    .frequency(p.getFrequency())
                    .dosesPerDay(dosesPerDay)
                    .takenCount((int) taken)
                    .pendingCount(pending)
                    .lastTakenTime(lastTakenMap.get(p.getId()))
                    .build();
        }).toList();
    }
}
