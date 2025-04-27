package com.mygo.mapper;

import com.mygo.entity.User;
import com.mygo.enumeration.UserStatus;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.*;

public interface UserMapper {

    @Insert("INSERT INTO user(user_id, email, password) VALUES (#{id}, #{email}, #{password})")
    void addUser(@Param("id") long id, @Param("email") String email, @Param("password") String password);

    @Select("SELECT * FROM user WHERE email=#{email}")
    @Result(property = "status", column = "status", javaType = UserStatus.class, typeHandler =
            EnumTypeHandler.class)
    User selectUserByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE user_id=#{userId}")
    @Result(property = "status", column = "status", javaType = UserStatus.class, typeHandler =
            EnumTypeHandler.class)
    User selectUserById(Integer userId);

    @Update("UPDATE user SET phone=#{phone} and gender=#{gender} and age=#{age} and " +
            "emergency_contact=#{emergencyContact} and emergency_contact_phone=#{emergencyContactPhone} WHERE " +
            "user_id=#{id}")
    void updateInfo(String phone, String gender, Integer age, String emergencyContact, String emergencyContactPhone);

}
