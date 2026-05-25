package com.yian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yian.entity.*;
import com.yian.enums.BedStatusEnum;
import com.yian.enums.CareLevelStatusEnum;
import com.yian.enums.ResidentStatusEnum;
import com.yian.mapper.*;
import com.yian.service.DashboardService;
import com.yian.vo.DashboardStatsVO;
import com.yian.vo.DashboardStatsVO.DistributionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String INTENSIVE_CARE_CODE = "INTENSIVE";

    private final ResidentMapper residentMapper;
    private final ResidentCareLevelMapper residentCareLevelMapper;
    private final CareLevelMapper careLevelMapper;
    private final BedMapper bedMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsVO getStats() {
        long totalResidents = residentMapper.selectCount(null);

        long checkedInCount = residentMapper.selectCount(
                new LambdaQueryWrapper<Resident>()
                        .eq(Resident::getStatus, ResidentStatusEnum.CHECKED_IN.getCode()));

        long specialCareCount = countSpecialCareResidents();

        long todayNewCount = residentMapper.selectCount(
                new LambdaQueryWrapper<Resident>()
                        .ge(Resident::getCreatedAt, LocalDate.now().atStartOfDay()));

        String occupancyRate = calcOccupancyRate();

        List<DistributionItem> careLevelDistribution = buildCareLevelDistribution();

        List<DistributionItem> ageDistribution = buildAgeDistribution();

        return DashboardStatsVO.builder()
                .totalResidents(totalResidents)
                .checkedInCount(checkedInCount)
                .specialCareCount(specialCareCount)
                .todayNewCount(todayNewCount)
                .occupancyRate(occupancyRate)
                .careLevelDistribution(careLevelDistribution)
                .ageDistribution(ageDistribution)
                .build();
    }

    /**
     * 统计"特殊护理"人数 — INTENSIVE 是 levelCode 而非主键，需先查 CareLevel 表获取 ID。
     */
    private long countSpecialCareResidents() {
        List<CareLevel> intensive = careLevelMapper.selectList(
                new LambdaQueryWrapper<CareLevel>().eq(CareLevel::getLevelCode, INTENSIVE_CARE_CODE));
        if (intensive.isEmpty()) {
            return 0;
        }
        Long intensiveId = intensive.get(0).getId();
        return residentCareLevelMapper.selectCount(
                new LambdaQueryWrapper<ResidentCareLevel>()
                        .eq(ResidentCareLevel::getCareLevelId, intensiveId)
                        .eq(ResidentCareLevel::getStatus, CareLevelStatusEnum.ACTIVE.getCode()));
    }

    private String calcOccupancyRate() {
        long totalBeds = bedMapper.selectCount(null);
        // 没有床位时避免除以零
        if (totalBeds == 0) {
            return "0%";
        }
        long occupied = bedMapper.selectCount(
                new LambdaQueryWrapper<Bed>()
                        .eq(Bed::getStatus, BedStatusEnum.OCCUPIED.getCode()));
        return String.format("%.1f%%", (double) occupied / totalBeds * 100);
    }

    /**
     * 构建护理级别分布 — 一次性查出所有 active 记录，在内存中按 careLevelId 分组统计，
     * 避免对每个护理级别各发一次 COUNT 查询。
     */
    private List<DistributionItem> buildCareLevelDistribution() {
        List<CareLevel> levels = careLevelMapper.selectList(
                new LambdaQueryWrapper<CareLevel>().orderByAsc(CareLevel::getSortOrder));

        Set<Long> levelIds = levels.stream().map(CareLevel::getId).collect(Collectors.toSet());
        final Map<Long, Long> countMap;
        if (!levelIds.isEmpty()) {
            List<ResidentCareLevel> activeRecords = residentCareLevelMapper.selectList(
                    new LambdaQueryWrapper<ResidentCareLevel>()
                            .in(ResidentCareLevel::getCareLevelId, levelIds)
                            .eq(ResidentCareLevel::getStatus, CareLevelStatusEnum.ACTIVE.getCode()));
            countMap = activeRecords.stream()
                    .collect(Collectors.groupingBy(ResidentCareLevel::getCareLevelId, Collectors.counting()));
        } else {
            countMap = Map.of();
        }

        return levels.stream().map(cl -> DistributionItem.builder()
                .name(cl.getLevelName())
                .value(countMap.getOrDefault(cl.getId(), 0L))
                .build()).toList();
    }

    private List<DistributionItem> buildAgeDistribution() {
        List<Resident> residents = residentMapper.selectList(
                new LambdaQueryWrapper<Resident>().isNotNull(Resident::getAge));

        Map<String, Long> ageMap = new LinkedHashMap<>();
        ageMap.put("60岁以下", 0L);
        ageMap.put("60-69岁", 0L);
        ageMap.put("70-79岁", 0L);
        ageMap.put("80-89岁", 0L);
        ageMap.put("90岁以上", 0L);

        for (Resident r : residents) {
            int age = r.getAge() != null ? r.getAge() : 0;
            if (age < 60) {
                ageMap.merge("60岁以下", 1L, Long::sum);
            } else if (age < 70) {
                ageMap.merge("60-69岁", 1L, Long::sum);
            } else if (age < 80) {
                ageMap.merge("70-79岁", 1L, Long::sum);
            } else if (age < 90) {
                ageMap.merge("80-89岁", 1L, Long::sum);
            } else {
                ageMap.merge("90岁以上", 1L, Long::sum);
            }
        }

        return ageMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> DistributionItem.builder().name(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());
    }
}
