package com.mygo.mapper;

import com.mygo.domain.enumeration.MessageType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ChatMapper {

    @Select("select * from consult_record where admin_id=#{adminId} and user_id=#{userId} and status='progressing'")
    Integer getConsultId(Integer adminId, Integer userId);

    @Update("insert into message(consult_id,message,message_type,sender) values (#{consultId},#{message},#{messageType},#{sender})")
    void addMessage(Integer consultId, String message, MessageType messageType,String sender);
}
