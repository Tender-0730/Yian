package com.yian.service;

import com.yian.dto.ChangePasswordRequest;
import com.yian.dto.UpdateProfileRequest;
import com.yian.vo.UserProfileVO;

public interface UserService {

    UserProfileVO getProfile();

    void updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);
}
