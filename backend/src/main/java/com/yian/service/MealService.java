package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.MealQuery;
import com.yian.dto.MealSaveRequest;
import com.yian.vo.DietaryRestrictionVO;
import com.yian.vo.MealRecordVO;

import java.time.LocalDate;
import java.util.List;

public interface MealService {

    PageResult<MealRecordVO> pageMeals(MealQuery query);

    List<MealRecordVO> getDailyMeals(LocalDate date);

    Long createMeal(MealSaveRequest request);

    void updateMeal(Long id, MealSaveRequest request);

    void deleteMeal(Long id);

    List<DietaryRestrictionVO> getResidentRestrictions(Long residentId);
}
