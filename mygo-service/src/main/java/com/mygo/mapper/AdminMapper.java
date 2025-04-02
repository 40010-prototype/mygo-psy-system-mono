package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.dto.UserDTO;
import com.mygo.domain.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE name=#{name}")
    Admin getAdminByName(@Param("name") String name);

    @Insert("INSERT INTO admin(name, password, email) VALUES(#{name}, #{password}, #{email})")
    void addAdmin(@Param("name") String name, String password, String email);

    @Select("SELECT email FROM admin WHERE name=#{name}")
    String getEmailByName(String name);

    @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
    void updatePassword(@Param("name") String name, @Param("password") String password);

    @Select("SELECT id,name,email,phone,role FROM admin WHERE id=#{id}")
    UserDTO getUserDTOById(long id);
}
