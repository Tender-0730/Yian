package com.yian.service.impl;

import com.yian.common.BusinessException;
import com.yian.common.ResultCode;
import com.yian.dto.LoginRequest;
import com.yian.dto.RefreshTokenRequest;
import com.yian.dto.RegisterRequest;
import com.yian.entity.SysRole;
import com.yian.entity.SysUser;
import com.yian.entity.SysUserRole;
import com.yian.mapper.SysRoleMapper;
import com.yian.mapper.SysUserMapper;
import com.yian.mapper.SysUserRoleMapper;
import com.yian.security.JwtUtils;
import com.yian.security.LoginUser;
import com.yian.service.AuthService;
import com.yian.vo.LoginVO;
import com.yian.vo.UserInfoVO;
import com.yian.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("登录失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(
                loginUser.getUserId(), loginUser.getUsername(), loginUser.getRoleCodes());
        String refreshToken = jwtUtils.generateRefreshToken(loginUser.getUserId(), loginUser.getUsername());

        SysUser updateUser = new SysUser();
        updateUser.setId(loginUser.getUserId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(updateUser);

        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        UserInfoVO userInfo = UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roleCodes(loginUser.getRoleCodes())
                .build();

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (sysUserMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysRole staffRole = sysRoleMapper.selectByRoleCode("STAFF");
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(staffRole.getId());
        sysUserRoleMapper.insert(userRole);

        log.info("新用户注册成功: {}", request.getUsername());
    }

    @Override
    public LoginVO refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            jwtUtils.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "refreshToken无效");
        }

        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "非法的token类型");
        }
        if (jwtUtils.isTokenExpired(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "refreshToken已过期");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户不存在或已被禁用");
        }

        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(userId);
        String newAccessToken = jwtUtils.generateAccessToken(userId, username, roleCodes);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);

        UserInfoVO userInfo = UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roleCodes(roleCodes)
                .build();

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public UserInfoVO me() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roleCodes(loginUser.getRoleCodes())
                .build();
    }
}
