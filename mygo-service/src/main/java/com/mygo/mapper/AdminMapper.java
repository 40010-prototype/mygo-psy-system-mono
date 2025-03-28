package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("select * from admins where name=#{name}")
    Admin getAdminByName(@Param("name") String name);

    @Insert("insert into admins(name,password,email) values(#{username},#{password},#{email})")
    void addAdmin(String username, String password, String email);
}