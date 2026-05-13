package com.yian.service.impl;

import com.yian.common.BusinessException;
import com.yian.dto.ChangePasswordRequest;
import com.yian.dto.UpdateProfileRequest;
import com.yian.entity.SysUser;
import com.yian.mapper.SysUserMapper;
import com.yian.mapper.SysUserRoleMapper;
import com.yian.security.LoginUser;
import com.yian.service.UserService;
import com.yian.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileVO getProfile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        return UserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .roleCodes(loginUser.getRoleCodes())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        LoginUser loginUser = getLoginUser();
        SysUser user = new SysUser();
        user.setId(loginUser.getUserId());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        sysUserMapper.updateById(user);
        log.info("更新用户资料成功: userId={}", loginUser.getUserId());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        LoginUser loginUser = getLoginUser();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(loginUser.getUserId());
        updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(updateUser);
        log.info("修改密码成功: userId={}", loginUser.getUserId());
    }

    private LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
