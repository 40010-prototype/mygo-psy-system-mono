package com.mygo.mapper;

import com.mygo.dto.LastMessageAndTime;
import com.mygo.entity.Consult;
import com.mygo.enumeration.ConsultStatus;
import com.mygo.enumeration.MessageStatus;
import com.mygo.enumeration.MessageType;
import com.mygo.enumeration.UserStatus;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

public interface ChatMapper {

    @Select("SELECT consult_id FROM consult_record WHERE participant1_admin_id=#{adminId} AND participant2_user_id=#{userId} AND " +
            "status='progressing'")
    Integer getConsultId(Integer adminId, Integer userId);

    @Update("INSERT INTO message(consult_id, message, message_type, sender) VALUES (#{consultId}, #{message}, " +
            "#{messageType}, #{sender})")
    void addMessage(Integer consultId, String message, MessageType messageType, String sender);

    @Select("select count(*) from message where consult_id=#{consultId} and status='delivered'")
    Integer getUnreadMessageCount(Integer consultId, MessageType messageType);

    @Select("select message,time from message where consult_id=#{consultId} order by time desc limit 1")
    @Result(property = "status", column = "status", javaType = MessageStatus.class, typeHandler =
            EnumTypeHandler.class)
    LastMessageAndTime getLastMessage(Integer consultId);

    @Select("select * from consult_record where consult_id=#{consultId}")
    @Result(property = "status", column = "status", javaType = ConsultStatus.class, typeHandler =
            EnumTypeHandler.class)
    Consult getConsultById(Integer consultId);

    Integer getMessageId(Integer adminId, Integer userId);

    @Select("select status from user where user_id=#{userId}")
    @Result(property = "status", column = "status", javaType = UserStatus.class, typeHandler = EnumTypeHandler.class)
    UserStatus getUserStatus(Integer userId);

    @Select("select count(*) from consult_record where participant2_user_id=#{userId} and participant1_admin_id=#{counselorId}")
    Integer checkConsultExists(Integer userId, Integer counselorId);

    @Insert("insert into consult_record(participant2_user_id,participant1_admin_id) value(#{userId},#{counselorId})")
    void addConsult(Integer userId, Integer counselorId);



    @Select("select consult_id from consult_record where participant2_user_id=#{userId} and participant1_admin_id=#{counselorId}")
    Integer getConsult(Integer userId, Integer counselorId);

    @Select("select admin_id from admin where admin_id=#{toId}")
    Integer getRole(Integer toId);

    @Delete("delete from consult_record where participant2_user_id=#{userId} and participant1_admin_id=#{counselorId}")
    void removeConsult(Integer userId, Integer counselorId);

    // 新增逻辑
    // 获取督导-咨询师会话ID
    @Select("select consult_id from consult_record where participant1_admin_id=#{supervisorId} and participant2_admin_id=#{counselorId}")
    Integer getConsultSupervisorCounselor(Integer supervisorId, Integer counselorId);

    // 添加 Admin-User (咨询师-用户) 类型的会话记录
    @Insert("INSERT INTO consult_record (participant1_admin_id, participant2_user_id, participant2_admin_id) " +
            "VALUES (#{counselorId}, #{userId}, NULL)")
    void addConsultAdminUser(Integer userId, Integer counselorId);

    // 添加 Admin-Admin (督导-咨询师) 类型的会话记录
    // 约定：督导作为 Participant 1，咨询师作为 Participant 2
    @Insert("INSERT INTO consult_record (" +
            "participant1_admin_id, " + // 督导 ID
            "participant2_user_id, " + // 必须为 NULL
            "participant2_admin_id" + // 咨询师 ID
            ") VALUES (" +
            "#{supervisorId}, " + // 对应下面的 supervisorId 参数
            "NULL, " + // participant2 不是 user，设为 NULL
            "#{counselorId}" + // 对应下面的 counselorId 参数
            ")")
    void addConsultAdminAdmin(Integer supervisorId, Integer counselorId);

    @Select("select count(*) from consult_record where participant2_admin_id=#{userId} and participant1_admin_id=#{counselorId}")
    Integer checkSupevisorConsultExists(Integer userId, Integer counselorId);

    @Select("select count(*) from consult_record where participant2_admin_id=#{userId} and participant1_admin_id=#{counselorId}")
    Integer checkSupervisorConsultExists(Integer userId, Integer counselorId);

    @Delete("delete from consult_record where participant2_admin_id=#{userId} and participant1_admin_id=#{counselorId}")
    void removeSupervisorConsult(Integer userId, Integer counselorId);

}

