package com.mygo.mapper;

import com.mygo.dto.LastMessageAndTime;
import com.mygo.entity.Consult;
import com.mygo.enumeration.ConsultStatus;
import com.mygo.enumeration.MessageStatus;
import com.mygo.enumeration.MessageType;
import com.mygo.enumeration.UserStatus;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ChatMapper {

    @Select("SELECT consult_id FROM consult_record WHERE admin_id=#{adminId} AND user_id=#{userId} AND " +
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

}
