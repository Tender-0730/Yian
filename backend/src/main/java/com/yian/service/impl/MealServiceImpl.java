package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.MealQuery;
import com.yian.dto.MealSaveRequest;
import com.yian.entity.DietaryRestriction;
import com.yian.entity.MealRecord;
import com.yian.entity.Resident;
import com.yian.mapper.DietaryRestrictionMapper;
import com.yian.mapper.MealRecordMapper;
import com.yian.mapper.ResidentMapper;
import com.yian.service.MealService;
import com.yian.vo.DietaryRestrictionVO;
import com.yian.vo.MealRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRecordMapper mealRecordMapper;
    private final DietaryRestrictionMapper dietaryRestrictionMapper;
    private final ResidentMapper residentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<MealRecordVO> pageMeals(MealQuery query) {
        LambdaQueryWrapper<MealRecord> wrapper = new LambdaQueryWrapper<>();
        if (query.getResidentId() != null) {
            wrapper.eq(MealRecord::getResidentId, query.getResidentId());
        }
        if (query.getMealDate() != null) {
            wrapper.eq(MealRecord::getMealDate, query.getMealDate());
        }
        if (query.getMealType() != null) {
            wrapper.eq(MealRecord::getMealType, query.getMealType());
        }
        wrapper.orderByDesc(MealRecord::getMealDate).orderByAsc(MealRecord::getMealType);

        Page<MealRecord> page = new Page<>(query.getPage(), query.getSize());
        Page<MealRecord> result = mealRecordMapper.selectPage(page, wrapper);
        List<MealRecord> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Map<Long, String> nameMap = buildResidentNameMap(records);

        List<MealRecordVO> vos = records.stream().map(r -> MealRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(nameMap.get(r.getResidentId()))
                .mealDate(r.getMealDate())
                .mealType(r.getMealType())
                .content(r.getContent())
                .notes(r.getNotes())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealRecordVO> getDailyMeals(LocalDate date) {
        List<MealRecord> records = mealRecordMapper.selectList(
                new LambdaQueryWrapper<MealRecord>()
                        .eq(MealRecord::getMealDate, date)
                        .orderByAsc(MealRecord::getMealType));

        Map<Long, String> nameMap = buildResidentNameMap(records);

        return records.stream().map(r -> MealRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(nameMap.get(r.getResidentId()))
                .mealDate(r.getMealDate())
                .mealType(r.getMealType())
                .content(r.getContent())
                .notes(r.getNotes())
                .build()).toList();
    }

    @Override
    @Transactional
    public Long createMeal(MealSaveRequest request) {
        if (residentMapper.selectById(request.getResidentId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "老人不存在");
        }

        MealRecord r = new MealRecord();
        r.setResidentId(request.getResidentId());
        r.setMealDate(request.getMealDate());
        r.setMealType(request.getMealType());
        r.setContent(request.getContent());
        r.setNotes(request.getNotes());
        mealRecordMapper.insert(r);
        log.info("新增膳食记录成功: id={}, residentId={}", r.getId(), r.getResidentId());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateMeal(Long id, MealSaveRequest request) {
        MealRecord existing = mealRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "膳食记录不存在");
        }

        MealRecord r = new MealRecord();
        r.setId(id);
        r.setResidentId(request.getResidentId());
        r.setMealDate(request.getMealDate());
        r.setMealType(request.getMealType());
        r.setContent(request.getContent());
        r.setNotes(request.getNotes());
        mealRecordMapper.updateById(r);
        log.info("更新膳食记录成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteMeal(Long id) {
        if (mealRecordMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "膳食记录不存在");
        }
        mealRecordMapper.deleteById(id);
        log.info("删除膳食记录成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DietaryRestrictionVO> getResidentRestrictions(Long residentId) {
        List<DietaryRestriction> restrictions = dietaryRestrictionMapper.selectByResidentId(residentId);
        return restrictions.stream().map(r -> DietaryRestrictionVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .restriction(r.getRestriction())
                .description(r.getDescription())
                .build()).toList();
    }

    /**
     * 批量构建 residentId → name 映射，避免分页列表中逐条查询老人姓名（N+1）。
     */
    private Map<Long, String> buildResidentNameMap(List<MealRecord> records) {
        Set<Long> residentIds = records.stream()
                .map(MealRecord::getResidentId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(residentIds)) {
            return Map.of();
        }
        List<Resident> residents = residentMapper.selectNameByIds(
                residentIds.stream().toList());
        return residents.stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));
    }
}
