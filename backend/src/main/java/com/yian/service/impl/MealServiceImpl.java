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
import com.yian.mapper.DietaryRestrictionMapper;
import com.yian.mapper.MealRecordMapper;
import com.yian.mapper.ResidentMapper;
import com.yian.service.MealService;
import com.yian.vo.MealRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRecordMapper mealRecordMapper;
    private final DietaryRestrictionMapper dietaryRestrictionMapper;
    private final ResidentMapper residentMapper;

    @Override
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

        List<MealRecordVO> vos = records.stream().map(r -> MealRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(residentMapper.selectNameById(r.getResidentId()))
                .mealDate(r.getMealDate())
                .mealType(r.getMealType())
                .content(r.getContent())
                .notes(r.getNotes())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public List<MealRecordVO> getDailyMeals(LocalDate date) {
        List<MealRecord> records = mealRecordMapper.selectList(
                new LambdaQueryWrapper<MealRecord>()
                        .eq(MealRecord::getMealDate, date)
                        .orderByAsc(MealRecord::getMealType));
        return records.stream().map(r -> MealRecordVO.builder()
                .id(r.getId())
                .residentId(r.getResidentId())
                .residentName(residentMapper.selectNameById(r.getResidentId()))
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
    public List<DietaryRestriction> getResidentRestrictions(Long residentId) {
        return dietaryRestrictionMapper.selectByResidentId(residentId);
    }
}
