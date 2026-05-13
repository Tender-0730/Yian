package com.yian.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yian.entity.SysUser;
import com.yian.mapper.SysUserMapper;
import com.yian.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: {}", username);
            throw new DisabledException("用户已被禁用");
        }
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(user.getId());
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), roleCodes);
    }
}
