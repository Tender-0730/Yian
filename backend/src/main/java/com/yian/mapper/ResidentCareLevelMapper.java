package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.ResidentCareLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResidentCareLevelMapper extends BaseMapper<ResidentCareLevel> {

    @Select("SELECT * FROM resident_care_level WHERE resident_id = #{residentId} AND status = 'ACTIVE' LIMIT 1")
    ResidentCareLevel selectCurrentByResidentId(@Param("residentId") Long residentId);
}
