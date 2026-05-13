package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

    /**
     * 查询某老人当前入住中的记录（status=CHECKED_IN，一个老人同时只能有一条入住记录）。
     */
    @Select("SELECT * FROM check_in_record WHERE resident_id = #{residentId} AND status = 'CHECKED_IN' LIMIT 1")
    CheckInRecord selectCurrentByResidentId(@Param("residentId") Long residentId);

    /**
     * 查询某床位当前入住中的记录，用于判断床位是否已被占用。
     */
    @Select("SELECT * FROM check_in_record WHERE bed_id = #{bedId} AND status = 'CHECKED_IN' LIMIT 1")
    CheckInRecord selectCurrentByBedId(@Param("bedId") Long bedId);
}
