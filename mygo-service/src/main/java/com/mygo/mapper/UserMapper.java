package com.mygo.mapper;

import com.mygo.domain.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(user_id, email, password) VALUES (#{id}, #{email}, #{password})")
    void addUser(@Param("id") long id, @Param("email") String email, @Param("password") String password);

    @Select("SELECT * FROM user WHERE email=#{email}")
    User selectUserByEmail(@Param("email") String email);

    @Update("UPDATE user SET phone=#{phone} and gender=#{gender} and age=#{age} and emergency_contact=#{emergencyContact} and emergency_contact_phone=#{emergencyContactPhone} WHERE id=#{id}")
    void updateInfo(String phone,String gender,Integer age,String emergencyContact,String emergencyContactPhone);
}
