package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username=#{username}")
    public User getUserByUsername(@Param("username") String username);
}