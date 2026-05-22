package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.*;
import com.yian.vo.NurseInfoVO;
import com.yian.vo.NurseScheduleVO;
import com.yian.vo.ShiftHandoverVO;

public interface NurseService {

    // 护工信息
    PageResult<NurseInfoVO> pageNurses(NurseQuery query);
    NurseInfoVO getNurseById(Long id);
    Long createNurse(NurseInfoSaveRequest request);
    void updateNurse(Long id, NurseInfoSaveRequest request);
    void deleteNurse(Long id);

    // 排班
    PageResult<NurseScheduleVO> pageSchedules(NurseQuery query);
    Long createSchedule(NurseScheduleSaveRequest request);
    void updateSchedule(Long id, NurseScheduleSaveRequest request);
    void deleteSchedule(Long id);

    // 交接班
    PageResult<ShiftHandoverVO> pageHandovers(NurseQuery query);
    Long createHandover(ShiftHandoverSaveRequest request);
}
