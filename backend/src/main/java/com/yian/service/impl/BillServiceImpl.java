package com.yian.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yian.common.BusinessException;
import com.yian.common.PageResult;
import com.yian.common.ResultCode;
import com.yian.dto.*;
import com.yian.entity.*;
import com.yian.enums.BillStatusEnum;
import com.yian.enums.ChargeUnitEnum;
import com.yian.enums.ResidentStatusEnum;
import com.yian.mapper.*;
import com.yian.security.LoginUser;
import com.yian.service.BillService;
import com.yian.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final FeeConfigMapper feeConfigMapper;
    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final ResidentMapper residentMapper;

    // ==================== 费用配置 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<FeeConfigVO> pageFeeConfigs(FeeQuery query) {
        LambdaQueryWrapper<FeeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FeeConfig::getSortOrder);

        Page<FeeConfig> page = new Page<>(query.getPage(), query.getSize());
        Page<FeeConfig> result = feeConfigMapper.selectPage(page, wrapper);
        List<FeeConfig> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        List<FeeConfigVO> vos = records.stream().map(r -> FeeConfigVO.builder()
                .id(r.getId()).feeName(r.getFeeName()).defaultAmount(r.getDefaultAmount())
                .chargeUnit(r.getChargeUnit()).description(r.getDescription())
                .sortOrder(r.getSortOrder()).status(r.getStatus()).createdAt(r.getCreatedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createFeeConfig(FeeConfigSaveRequest request) {
        FeeConfig r = new FeeConfig();
        r.setFeeName(request.getFeeName());
        r.setDefaultAmount(request.getDefaultAmount());
        r.setChargeUnit(request.getChargeUnit());
        r.setDescription(request.getDescription());
        r.setSortOrder(request.getSortOrder());
        r.setStatus("ACTIVE");
        feeConfigMapper.insert(r);
        log.info("新增费用配置成功: id={}, name={}", r.getId(), r.getFeeName());
        return r.getId();
    }

    @Override
    @Transactional
    public void updateFeeConfig(Long id, FeeConfigSaveRequest request) {
        if (feeConfigMapper.selectById(id) == null)
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "费用配置不存在");
        FeeConfig r = new FeeConfig();
        r.setId(id);
        r.setFeeName(request.getFeeName());
        r.setDefaultAmount(request.getDefaultAmount());
        r.setChargeUnit(request.getChargeUnit());
        r.setDescription(request.getDescription());
        r.setSortOrder(request.getSortOrder());
        feeConfigMapper.updateById(r);
        log.info("更新费用配置成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteFeeConfig(Long id) {
        if (feeConfigMapper.selectById(id) == null)
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "费用配置不存在");
        feeConfigMapper.deleteById(id);
        log.info("删除费用配置成功: id={}", id);
    }

    // ==================== 账单 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<BillVO> pageBills(FeeQuery query) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        if (query.getResidentId() != null) {
            wrapper.eq(Bill::getResidentId, query.getResidentId());
        }
        if (query.getBillPeriod() != null) {
            wrapper.eq(Bill::getBillPeriod, query.getBillPeriod());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Bill::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Bill::getCreatedAt);

        Page<Bill> page = new Page<>(query.getPage(), query.getSize());
        Page<Bill> result = billMapper.selectPage(page, wrapper);
        List<Bill> records = result.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageResult.of(List.of(), 0, query.getPage(), query.getSize());
        }

        Set<Long> residentIds = records.stream().map(Bill::getResidentId).collect(Collectors.toSet());
        Map<Long, String> residentNameMap = residentMapper.selectNameByIds(new ArrayList<>(residentIds)).stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));

        List<BillVO> vos = records.stream().map(r -> BillVO.builder()
                .id(r.getId()).residentId(r.getResidentId()).residentName(residentNameMap.get(r.getResidentId()))
                .billNo(r.getBillNo()).billPeriod(r.getBillPeriod())
                .totalAmount(r.getTotalAmount()).paidAmount(r.getPaidAmount())
                .unpaidAmount(r.getTotalAmount().subtract(r.getPaidAmount()))
                .status(r.getStatus()).remark(r.getRemark()).generatedAt(r.getGeneratedAt())
                .build()).toList();

        return PageResult.of(vos, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public BillDetailVO getBillDetail(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "账单不存在");

        String residentName = residentMapper.selectNameById(bill.getResidentId());

        List<BillItem> items = billItemMapper.selectList(
                new LambdaQueryWrapper<BillItem>().eq(BillItem::getBillId, id));
        List<BillItemVO> itemVOs = items.stream().map(i -> BillItemVO.builder()
                .id(i.getId()).billId(i.getBillId()).feeConfigId(i.getFeeConfigId())
                .feeName(i.getFeeName()).amount(i.getAmount()).description(i.getDescription())
                .build()).toList();

        List<PaymentRecord> payments = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getBillId, id)
                        .orderByDesc(PaymentRecord::getPaidAt));
        List<PaymentRecordVO> paymentVOs = payments.stream().map(p -> PaymentRecordVO.builder()
                .id(p.getId()).billId(p.getBillId()).residentId(p.getResidentId())
                .amount(p.getAmount()).paymentMethod(p.getPaymentMethod())
                .paidAt(p.getPaidAt()).receivedBy(p.getReceivedBy()).remark(p.getRemark())
                .build()).toList();

        return BillDetailVO.builder()
                .id(bill.getId()).residentId(bill.getResidentId()).residentName(residentName)
                .billNo(bill.getBillNo()).billPeriod(bill.getBillPeriod())
                .totalAmount(bill.getTotalAmount()).paidAmount(bill.getPaidAmount())
                .unpaidAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()))
                .status(bill.getStatus()).remark(bill.getRemark()).generatedAt(bill.getGeneratedAt())
                .items(itemVOs).payments(paymentVOs)
                .build();
    }

    @Override
    @Transactional
    public int generateBills(BillGenerateRequest request) {
        // 解析月份
        YearMonth ym = YearMonth.parse(request.getBillPeriod());

        // 查询在院老人（或指定老人）
        LambdaQueryWrapper<Resident> residentWrapper = new LambdaQueryWrapper<>();
        if (request.getResidentId() != null) {
            residentWrapper.eq(Resident::getId, request.getResidentId());
        } else {
            residentWrapper.eq(Resident::getStatus, ResidentStatusEnum.CHECKED_IN.getCode());
        }
        List<Resident> residents = residentMapper.selectList(residentWrapper);
        if (CollUtil.isEmpty(residents)) return 0;

        // 查询所有启用状态的费用配置
        List<FeeConfig> feeConfigs = feeConfigMapper.selectList(
                new LambdaQueryWrapper<FeeConfig>().eq(FeeConfig::getStatus, "ACTIVE"));
        if (CollUtil.isEmpty(feeConfigs)) return 0;

        int totalDaysOfMonth = ym.lengthOfMonth();
        int count = 0;

        for (Resident resident : residents) {
            // 计算当月有效天数（处理同月入院+退住的场景）
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            int effectiveDays = totalDaysOfMonth;

            boolean admittedInMonth = resident.getAdmissionDate() != null
                    && !resident.getAdmissionDate().isBefore(monthStart)
                    && !resident.getAdmissionDate().isAfter(monthEnd);
            boolean dischargedInMonth = ResidentStatusEnum.CHECKED_OUT.getCode().equals(resident.getStatus())
                    && resident.getDischargeDate() != null
                    && !resident.getDischargeDate().isBefore(monthStart)
                    && !resident.getDischargeDate().isAfter(monthEnd);

            if (admittedInMonth && dischargedInMonth) {
                effectiveDays = (int) ChronoUnit.DAYS.between(resident.getAdmissionDate(), resident.getDischargeDate()) + 1;
            } else if (admittedInMonth) {
                effectiveDays = (int) ChronoUnit.DAYS.between(resident.getAdmissionDate(), monthEnd) + 1;
            } else if (dischargedInMonth) {
                effectiveDays = (int) ChronoUnit.DAYS.between(monthStart, resident.getDischargeDate()) + 1;
            }
            if (effectiveDays <= 0) continue;

            // 幂等检查
            boolean exists = billMapper.selectCount(
                    new LambdaQueryWrapper<Bill>()
                            .eq(Bill::getResidentId, resident.getId())
                            .eq(Bill::getBillPeriod, request.getBillPeriod())) > 0;
            if (exists) continue;

            // 生成账单号
            String billNo = "BILL" + request.getBillPeriod().replace("-", "")
                    + String.format("%04d", resident.getId());

            // 计算总金额
            BigDecimal total = BigDecimal.ZERO;
            List<BillItem> items = new ArrayList<>();

            for (FeeConfig fc : feeConfigs) {
                BigDecimal amount = fc.getDefaultAmount();
                if (ChargeUnitEnum.MONTHLY.getCode().equals(fc.getChargeUnit()) && effectiveDays < totalDaysOfMonth) {
                    // 按月计费 + 中途入住/退住 → 按天折算
                    amount = fc.getDefaultAmount()
                            .multiply(BigDecimal.valueOf(effectiveDays))
                            .divide(BigDecimal.valueOf(totalDaysOfMonth), 2, RoundingMode.HALF_UP);
                } else if (ChargeUnitEnum.DAILY.getCode().equals(fc.getChargeUnit())) {
                    amount = fc.getDefaultAmount().multiply(BigDecimal.valueOf(effectiveDays));
                }

                BillItem item = new BillItem();
                item.setFeeConfigId(fc.getId());
                item.setFeeName(fc.getFeeName());
                item.setAmount(amount);
                item.setDescription(fc.getDescription());
                items.add(item);
                total = total.add(amount);
            }

            // 创建账单
            Bill bill = new Bill();
            bill.setResidentId(resident.getId());
            bill.setBillNo(billNo);
            bill.setBillPeriod(request.getBillPeriod());
            bill.setTotalAmount(total);
            bill.setPaidAmount(BigDecimal.ZERO);
            bill.setStatus(BillStatusEnum.UNPAID.getCode());
            bill.setGeneratedAt(LocalDateTime.now());
            try {
                billMapper.insert(bill);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                log.warn("账单已存在（并发冲突），跳过: residentId={}, billPeriod={}",
                        resident.getId(), request.getBillPeriod());
                continue;
            }

            // 创建明细
            for (BillItem item : items) {
                item.setBillId(bill.getId());
                billItemMapper.insert(item);
            }
            count++;
        }

        log.info("生成账单完成: billPeriod={}, count={}", request.getBillPeriod(), count);
        return count;
    }

    @Override
    @Transactional
    public void cancelBill(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "账单不存在");
        if (!BillStatusEnum.UNPAID.getCode().equals(bill.getStatus()))
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅未支付的账单可作废");

        long paymentCount = paymentRecordMapper.selectCount(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getBillId, id));
        if (paymentCount > 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已有缴费记录的账单不可作废");

        bill.setStatus(BillStatusEnum.CANCELLED.getCode());
        billMapper.updateById(bill);
        log.info("作废账单成功: id={}", id);
    }

    // ==================== 缴费 ====================

    @Override
    @Transactional
    public void payBill(Long id, PaymentSaveRequest request) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "账单不存在");
        if (BillStatusEnum.CANCELLED.getCode().equals(bill.getStatus()))
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已作废的账单不可缴费");
        if (BillStatusEnum.PAID.getCode().equals(bill.getStatus()))
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已付清的账单不可重复缴费");

        BigDecimal unpaid = bill.getTotalAmount().subtract(bill.getPaidAmount());
        if (request.getAmount().compareTo(unpaid) > 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                    "支付金额不能超过未付金额（" + unpaid + "）");

        PaymentRecord pr = new PaymentRecord();
        pr.setBillId(id);
        pr.setResidentId(bill.getResidentId());
        pr.setAmount(request.getAmount());
        pr.setPaymentMethod(request.getPaymentMethod());
        pr.setPaidAt(LocalDateTime.now());
        pr.setReceivedBy(getCurrentUserId());
        pr.setRemark(request.getRemark());
        paymentRecordMapper.insert(pr);

        bill.setPaidAmount(bill.getPaidAmount().add(request.getAmount()));
        if (bill.getPaidAmount().compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus(BillStatusEnum.PAID.getCode());
        } else {
            bill.setStatus(BillStatusEnum.PARTIAL.getCode());
        }
        billMapper.updateById(bill);
        log.info("缴费成功: billId={}, amount={}", id, request.getAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentArrearsVO> listArrears() {
        List<Bill> unpaidBills = billMapper.selectList(
                new LambdaQueryWrapper<Bill>()
                        .in(Bill::getStatus, BillStatusEnum.UNPAID.getCode(), BillStatusEnum.PARTIAL.getCode()));

        if (CollUtil.isEmpty(unpaidBills)) return List.of();

        Set<Long> residentIds = unpaidBills.stream().map(Bill::getResidentId).collect(Collectors.toSet());
        Map<Long, String> nameMap = residentMapper.selectNameByIds(new ArrayList<>(residentIds)).stream()
                .collect(Collectors.toMap(Resident::getId, Resident::getName));

        Map<Long, BigDecimal> arrearsMap = new LinkedHashMap<>();
        Map<Long, Integer> countMap = new HashMap<>();
        for (Bill b : unpaidBills) {
            BigDecimal arrears = b.getTotalAmount().subtract(b.getPaidAmount());
            arrearsMap.merge(b.getResidentId(), arrears, BigDecimal::add);
            countMap.merge(b.getResidentId(), 1, Integer::sum);
        }

        return arrearsMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(e -> ResidentArrearsVO.builder()
                        .residentId(e.getKey()).residentName(nameMap.get(e.getKey()))
                        .totalArrears(e.getValue()).unpaidBillCount(countMap.get(e.getKey()))
                        .build()).toList();
    }

    private static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user.getUserId();
        }
        return null;
    }
}
