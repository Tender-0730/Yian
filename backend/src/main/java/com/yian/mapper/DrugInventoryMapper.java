package com.yian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yian.entity.DrugInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {

    @Update("UPDATE drug_inventory SET quantity = quantity + #{delta} WHERE id = #{id} AND quantity + #{delta} >= 0")
    int atomicUpdateQuantity(@Param("id") Long id, @Param("delta") int delta);
}
