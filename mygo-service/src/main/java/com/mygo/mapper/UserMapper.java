package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username=#{username}")
    User getUserByUsername(@Param("username") String username);

    @Insert("insert into user(username,password,create_time,update_time)" +
            " values(#{username},#{password},now(),now())")
    void add(String username, String password);
}