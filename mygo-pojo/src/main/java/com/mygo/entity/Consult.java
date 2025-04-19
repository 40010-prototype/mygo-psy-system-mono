package com.mygo.entity;

import com.mygo.enumeration.ConsultStatus;
import lombok.Getter;

@Getter
public class Consult {

    private Integer consultId;

    private Integer userId;

    private Integer adminId;

    private Integer score;

    private ConsultStatus status;

}
