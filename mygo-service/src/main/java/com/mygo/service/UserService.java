package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.UserAddInfoDTO;
import com.mygo.dto.UserLoginDTO;
import com.mygo.dto.UserRegisterDTO;
import com.mygo.vo.ActiveCounselorVO;
import com.mygo.vo.UserLoginVO;
import com.mygo.vo.UserMessageVO;

import java.util.List;

public interface UserService {

    void register(UserRegisterDTO userRegisterDTO);

    UserLoginVO login(UserLoginDTO userLoginDTO);

    void addInfo(UserAddInfoDTO userAddInfoDTO);

    List<ActiveCounselorVO> getActiveCounselor();

    void setSession(Integer counselorId) throws JsonProcessingException;

    List<UserMessageVO> getMessages(Integer counselorId) throws JsonProcessingException;

}
