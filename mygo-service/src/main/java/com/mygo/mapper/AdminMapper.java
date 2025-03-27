package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("select * from admins where name=#{name}")
    Admin getAdminByName(@Param("name") String name);

    @Insert("insert into user(username,password,create_time,update_time)" +
            " values(#{username},#{password},now(),now())")
    void add(String username, String password);
}