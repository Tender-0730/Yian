package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    @Select("SELECT * FROM bed WHERE room_id = #{roomId}")
    List<Bed> selectByRoomId(@Param("roomId") Long roomId);
}
