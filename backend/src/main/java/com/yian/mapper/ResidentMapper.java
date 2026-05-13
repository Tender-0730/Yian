package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.Resident;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResidentMapper extends BaseMapper<Resident> {

    @Select("SELECT name FROM resident WHERE id = #{id} AND is_deleted = 0")
    String selectNameById(@Param("id") Long id);
}
