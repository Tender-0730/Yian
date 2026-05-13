package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.HealthRecordQuery;
import com.yian.dto.HealthRecordSaveRequest;
import com.yian.vo.HealthRecordVO;

public interface HealthRecordService {

    PageResult<HealthRecordVO> pageHealthRecords(HealthRecordQuery query);

    HealthRecordVO getHealthRecordById(Long id);

    Long createHealthRecord(HealthRecordSaveRequest request);

    void updateHealthRecord(Long id, HealthRecordSaveRequest request);

    void deleteHealthRecord(Long id);
}
