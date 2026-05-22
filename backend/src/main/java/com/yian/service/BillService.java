package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.*;
import com.yian.vo.*;

import java.util.List;

public interface BillService {

    // 费用配置
    PageResult<FeeConfigVO> pageFeeConfigs(FeeQuery query);
    Long createFeeConfig(FeeConfigSaveRequest request);
    void updateFeeConfig(Long id, FeeConfigSaveRequest request);
    void deleteFeeConfig(Long id);

    // 账单
    PageResult<BillVO> pageBills(FeeQuery query);
    BillDetailVO getBillDetail(Long id);
    int generateBills(BillGenerateRequest request);
    void cancelBill(Long id);

    // 缴费
    void payBill(Long id, PaymentSaveRequest request);
    List<ResidentArrearsVO> listArrears();
}
