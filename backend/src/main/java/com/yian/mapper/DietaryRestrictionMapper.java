package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.DietaryRestriction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DietaryRestrictionMapper extends BaseMapper<DietaryRestriction> {

    @Select("SELECT * FROM dietary_restriction WHERE resident_id = #{residentId}")
    List<DietaryRestriction> selectByResidentId(@Param("residentId") Long residentId);
}
