package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
}
