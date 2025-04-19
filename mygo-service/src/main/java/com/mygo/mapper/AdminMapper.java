package com.mygo.mapper;

import com.mygo.entity.Admin;
import com.mygo.entity.Consult;
import com.mygo.entity.Message;
import com.mygo.enumeration.ConsultStatus;
import com.mygo.enumeration.MessageStatus;
import com.mygo.enumeration.MessageType;
import com.mygo.enumeration.Role;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE account_name=#{accountName}")
    @Results({@Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class)
            , @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)})
    Admin getAdminByName(String accountName);

    @Select("SELECT * FROM admin WHERE admin_id=#{id}")
    @Results({@Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class)
            , @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)})
    Admin getAdminById(Integer id);

    @Insert("INSERT INTO admin(admin_id, account_name, real_name, email, password,role,info) VALUES(#{id}, " +
            "#{accountName},#{realName},  #{email},#{password}, #{role},#{info})")
    void addAdmin(Integer id, String accountName, String realName, String email, String password,
                  Role role, String info);

    @Select("SELECT email FROM admin WHERE account_name=#{accountName}")
    String getEmailByAccountName(String accountName);

    @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
    void updatePassword(String name, String password);

    @Select("select consult_id,user_id from consult where admin_id=#{adminId}")
    @Results({
            @Result(property = "status", column = "status", javaType = ConsultStatus.class, typeHandler =
                    EnumTypeHandler.class)
    })
    List<Consult> getConsultInfoByAdminId(Integer adminId);

    @Select("select * from message where session_id=#{sessionId} limit #{offset},#{limit}")
    @Results({
            @Result(property = "status", column = "status", javaType = MessageStatus.class, typeHandler =
                    EnumTypeHandler.class),
            @Result(property = "messageType", column = "message_type", javaType = MessageType.class, typeHandler =
                    EnumTypeHandler.class),
            @Result(property = "time", column = "time", javaType = LocalDateTime.class)
    })
    List<Message> getHistoryMessageBySessionId(Integer sessionId, Integer offset, Integer limit);

}
