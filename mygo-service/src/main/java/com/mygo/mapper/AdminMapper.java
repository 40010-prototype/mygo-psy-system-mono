package com.mygo.mapper;

import com.mygo.domain.dto.UserDTO;
import com.mygo.domain.entity.Admin;
import com.mygo.domain.enumeration.Role;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AdminMapper  {

    @Select("SELECT * FROM admin WHERE name=#{name}")
    Admin getAdminByName(String name);

    @Insert("INSERT INTO admin(admin_id, name, password, email, role) VALUES(#{id}, #{name}, #{password}, #{email}, #{role})")
    void addAdmin(long id, String name, String password, String email, Role role);

    @Select("SELECT email FROM admin WHERE name=#{name}")
    String getEmailByName(String name);

    @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
    void updatePassword(String name, String password);

    @Select("SELECT id, name, email, phone, role FROM admin WHERE id=#{id}")
    UserDTO getUserDTOById(long id);


}
