package com.yian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
