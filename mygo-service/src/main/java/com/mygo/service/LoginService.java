package com.mygo.service;

import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.User;

public interface LoginService {

    public String login(LoginDTO loginDTO);

    User findByUserName(String username);

    public void register(LoginDTO loginDTO);
}
