package com.mygo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

//Json序列化需要Getter
@Getter
@AllArgsConstructor
public class UserLoginVO {

    private String token;

    private boolean needCompleteInfo;

}
