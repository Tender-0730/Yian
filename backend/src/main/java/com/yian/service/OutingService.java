package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.OutingQuery;
import com.yian.dto.OutingSaveRequest;
import com.yian.vo.OutingRecordVO;

import java.util.List;

public interface OutingService {

    PageResult<OutingRecordVO> pageOutings(OutingQuery query);

    OutingRecordVO getOutingById(Long id);

    Long createOuting(OutingSaveRequest request);

    void returnOuting(Long id);

    void cancelOuting(Long id);

    List<OutingRecordVO> listOverdue();
}
