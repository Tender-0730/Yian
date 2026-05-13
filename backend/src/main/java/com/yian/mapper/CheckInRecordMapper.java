package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.CheckInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

    @Select("SELECT * FROM check_in_record WHERE resident_id = #{residentId} AND status = 'CHECKED_IN' LIMIT 1")
    CheckInRecord selectCurrentByResidentId(@Param("residentId") Long residentId);

    @Select("SELECT * FROM check_in_record WHERE bed_id = #{bedId} AND status = 'CHECKED_IN' LIMIT 1")
    CheckInRecord selectCurrentByBedId(@Param("bedId") Long bedId);
}
