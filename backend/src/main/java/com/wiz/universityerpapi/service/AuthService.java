package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.LoginRequestDTO;
import com.wiz.universityerpapi.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}
