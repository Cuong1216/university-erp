package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * Logout: blacklist JWT token hiện tại của user.
     * Token sẽ không thể dùng lại dù chưa hết hạn.
     *
     * @param bearerToken Token dưới dạng "Bearer xxx..." hoặc chỉ là token string
     */
    void logout(String bearerToken);
}
