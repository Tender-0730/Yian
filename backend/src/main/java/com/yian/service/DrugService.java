package com.yian.service;

import com.yian.common.PageResult;
import com.yian.dto.*;
import com.yian.vo.*;

import java.time.LocalDate;
import java.util.List;

public interface DrugService {

    // 药品字典
    PageResult<DrugDictVO> pageDrugDicts(DrugDictQuery query);
    DrugDictVO getDrugDictById(Long id);
    Long createDrugDict(DrugDictSaveRequest request);
    void updateDrugDict(Long id, DrugDictSaveRequest request);
    void deleteDrugDict(Long id);

    // 库存
    PageResult<DrugInventoryVO> pageInventories(DrugDictQuery query);
    void inbound(Long id, int quantity, String reason);
    void outbound(Long id, int quantity, String reason);
    List<DrugInventoryVO> listAlerts();

    // 处方
    PageResult<DrugPrescriptionVO> pagePrescriptions(DrugDictQuery query);
    Long createPrescription(DrugPrescriptionSaveRequest request);
    void updatePrescription(Long id, DrugPrescriptionSaveRequest request);

    // 服药记录
    PageResult<DrugRecordVO> pageDrugRecords(DrugDictQuery query);
    void createDrugRecord(DrugRecordSaveRequest request);
    List<PendingDrugVO> listPending(Long residentId, LocalDate date);
}
