package com.mygo.service;

import com.mygo.dto.UserAddInfoDTO;
import com.mygo.dto.UserLoginDTO;
import com.mygo.dto.UserRegisterDTO;
import com.mygo.vo.UserLoginVO;

public interface UserService {

    void register(UserRegisterDTO userRegisterDTO);

    UserLoginVO login(UserLoginDTO userLoginDTO);

    void addInfo(UserAddInfoDTO userAddInfoDTO);

}
