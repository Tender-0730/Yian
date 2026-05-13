package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.Resident;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResidentMapper extends BaseMapper<Resident> {

    @Select("SELECT name FROM resident WHERE id = #{id} AND is_deleted = 0")
    String selectNameById(@Param("id") Long id);

    /**
     * 批量按 ID 查询老人姓名，用于分页列表组装 VO 时一次性填充 residentName，避免 N+1。
     */
    @Select("<script>SELECT id, name FROM resident WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND is_deleted = 0</script>")
    List<Resident> selectNameByIds(@Param("ids") List<Long> ids);
}
