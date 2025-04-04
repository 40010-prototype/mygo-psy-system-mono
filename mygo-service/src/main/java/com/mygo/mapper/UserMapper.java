package com.mygo.mapper;

import com.mygo.domain.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Insert("insert into user(user_id,email,password) values (#{id},#{email},#{password})")
    void addUser(@Param("id") long id, @Param("email") String email, @Param("password") String password);

    @Select("select * from user where email=#{email}")
    User selectUserByEmail(@Param("email") String email);

}
