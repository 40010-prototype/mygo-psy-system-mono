package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.User;

public interface LoginService {

    public String login(LoginDTO loginDTO) throws JsonProcessingException;

    User findByUserName(String username);

    public void register(LoginDTO loginDTO);
}
