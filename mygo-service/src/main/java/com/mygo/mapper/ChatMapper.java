package com.mygo.mapper;

import com.mygo.dto.LastMessageAndTime;
import com.mygo.entity.Consult;
import com.mygo.enumeration.MessageType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ChatMapper {

    @Select("SELECT consult_id FROM consult_record WHERE admin_id=#{adminId} AND user_id=#{userId} AND status='progressing'")
    Integer getConsultId(Integer adminId, Integer userId);

    @Update("INSERT INTO message(consult_id, message, message_type, sender) VALUES (#{consultId}, #{message}, " +
            "#{messageType}, #{sender})")
    void addMessage(Integer consultId, String message, MessageType messageType, String sender);

    @Select("select count(*) from message where consult_id=#{consultId} and status='delivered'")
    Integer getUnreadMessageCount(Integer consultId, MessageType messageType);

    @Select("select message,time from message where consult_id=#{consultId} order by time desc limit 1")
    LastMessageAndTime getLastMessage(Integer consultId);

    @Select("select * from consult_record where consult_id=#{consultId}")
    Consult getConsultById(Integer consultId);

    Integer getMessageId(Integer adminId, Integer userId);

}
