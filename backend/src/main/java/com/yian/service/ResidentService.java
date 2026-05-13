package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.ResidentPageQuery;
import com.yian.dto.ResidentSaveRequest;
import com.yian.vo.CareLevelVO;
import com.yian.vo.ResidentDetailVO;
import com.yian.vo.ResidentVO;

import java.util.List;

public interface ResidentService {

    PageResult<ResidentVO> pageResidents(ResidentPageQuery query);

    ResidentDetailVO getResidentById(Long id);

    Long createResident(ResidentSaveRequest request);

    void updateResident(Long id, ResidentSaveRequest request);

    void deleteResident(Long id);

    List<CareLevelVO> listCareLevels();

    void changeCareLevel(Long residentId, Long careLevelId);
}
