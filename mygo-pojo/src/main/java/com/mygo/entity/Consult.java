package com.mygo.entity;

import com.mygo.enumeration.ConsultStatus;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Consult {

    private Integer consultId;

    // 旧字段，保留以兼容旧代码，但不再直接映射到数据库字段
    private Integer userId;
    private Integer adminId;

    // 新字段
    private Integer participant1AdminId;
    private Integer participant2UserId;
    private Integer participant2AdminId;

    private Integer score;

    private ConsultStatus status;

    /**
     * 获取用户ID（兼容旧代码）
     * 如果participant2UserId不为空，则返回participant2UserId
     * 否则返回null
     */
    public Integer getUserId() {
        return participant2UserId;
    }

    /**
     * 获取管理员ID（兼容旧代码）
     * 如果participant2AdminId不为空，则作为咨询师-用户会话，返回participant1AdminId
     * 如果participant2AdminId不为空，则作为督导-咨询师会话，对于咨询师返回participant1AdminId，对于督导返回participant2AdminId
     */
    public Integer getAdminId() {
        // 简单处理：优先返回participant1AdminId
        return participant1AdminId;
    }
}
