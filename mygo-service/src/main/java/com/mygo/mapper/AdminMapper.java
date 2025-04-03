package com.mygo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mygo.domain.dto.UserDTO;
import com.mygo.domain.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE name=#{name}")
    Admin getAdminByName(String name);

    @Insert("INSERT INTO admin(admin_id,name, password, email) VALUES(#{id},#{name}, #{password}, #{email})")
    void addAdmin(long id, String name, String password, String email);

    @Select("SELECT email FROM admin WHERE name=#{name}")
    String getEmailByName(String name);

    @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
    void updatePassword(String name, String password);

    @Select("SELECT id,name,email,phone,role FROM admin WHERE id=#{id}")
    UserDTO getUserDTOById(long id);

}
