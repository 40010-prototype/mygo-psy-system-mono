package com.mygo.mapper;

import com.mygo.dto.DateAndStatusDTO;
import com.mygo.dto.ScheduleStatusDTO;
import com.mygo.dto.StartAndEndTime;
import com.mygo.entity.Admin;
import com.mygo.entity.Consult;
import com.mygo.entity.Message;
import com.mygo.entity.TimeSlot;
import com.mygo.enumeration.*;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface AdminMapper {

        @Select("SELECT * FROM admin WHERE account_name=#{accountName}")
        @Results({ @Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class) })
        Admin getAdminByName(String accountName);

        @Select("SELECT * FROM admin WHERE admin_id=#{id}")
        @Results({ @Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class) })
        Admin getAdminById(Integer id);

        @Insert("INSERT INTO admin(admin_id, account_name, real_name, email, password,role,info) VALUES(#{id}, " +
                        "#{accountName},#{realName},  #{email},#{password}, #{role},#{info})")
        void addAdmin(Integer id, String accountName, String realName, String email, String password,
                        Role role, String info);

        @Select("SELECT email FROM admin WHERE account_name=#{accountName}")
        String getEmailByAccountName(String accountName);

        @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
        void updatePassword(String name, String password);

        @Select("SELECT c.consult_id, c.participant2_user_id AS user_id, c.status " + // 1. 加上 status
                        "FROM consult_record c " +
                        "WHERE c.participant1_admin_id = #{adminId} AND c.participant2_admin_id IS NULL") // 2. 加上类型过滤条件
        @Results({
                        @Result(property = "consultId", column = "consult_id", id = true), // 3. 显式映射 consultId
                        @Result(property = "participant2UserId", column = "user_id"), // 4. 显式映射 userId (来自别名)
                        @Result(property = "userId", column = "user_id"), // 5. 显式映射 userId (来自别名)
                        @Result(property = "status", column = "status", javaType = ConsultStatus.class, // 6. 映射 status
                                        typeHandler = EnumTypeHandler.class)
        })
        List<Consult> getConsultInfoByAdminId(Integer adminId);

        @Select("SELECT c.consult_id, c.participant1_admin_id AS user_id, c.status " + // 1. 加上 status
                        "FROM consult_record c " +
                        "WHERE c.participant2_admin_id = #{adminId} AND c.participant2_user_id IS NULL") // 2. 加上类型过滤条件
        @Results({
                        @Result(property = "consultId", column = "consult_id", id = true), // 3. 显式映射 consultId
                        @Result(property = "participant1AdminId", column = "user_id"), // 4. 显式映射 userId (来自别名)
                        @Result(property = "adminId", column = "user_id"), // 5. 显式映射 userId (来自别名)
                        @Result(property = "status", column = "status", javaType = ConsultStatus.class, // 6. 映射 status
                                        typeHandler = EnumTypeHandler.class)
        })
        List<Consult> getSupervisorConsultInfoByCounselorId(Integer adminId);

        @Select("SELECT c.consult_id, c.participant2_admin_id AS user_id, c.status " + // 1. 加上 status
                        "FROM consult_record c " +
                        "WHERE c.participant1_admin_id = #{adminId} AND c.participant2_user_id IS NULL") // 2. 加上类型过滤条件
        @Results({
                        @Result(property = "consultId", column = "consult_id", id = true), // 3. 显式映射 consultId
                        @Result(property = "participant2AdminId", column = "user_id"), // 4. 显式映射 userId (来自别名)
                        @Result(property = "adminId", column = "user_id"), // 5. 显式映射 userId (来自别名)
                        @Result(property = "status", column = "status", javaType = ConsultStatus.class, // 6. 映射 status
                                        typeHandler = EnumTypeHandler.class)
        })
        List<Consult> getSupervisorConsultInfoBySupervisorId(Integer adminId);

        @Select("select * from message where consult_id=#{sessionId} order by time asc  ")
        @Results({
                        @Result(property = "status", column = "status", javaType = MessageStatus.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "messageType", column = "message_type", javaType = MessageType.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "sender", column = "sender", javaType = Sender.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "time", column = "time", javaType = LocalDateTime.class)
        })
        List<Message> getHistoryMessageBySessionId(Integer sessionId, Integer offset, Integer limit);

        @Update("update message set status='read' where consult_id=#{id}")
        void setRead(Integer id);

        @Select("select start_time,end_time from schedule where date=#{date} and admin_id=#{adminId}")
        List<StartAndEndTime> getTimePeriodByDateAndAdminId(LocalDate date, Integer adminId);

        @Insert("insert into schedule(admin_id,date,start_time,end_time,status) value (#{adminId},#{date},#{startTime},"
                        +
                        "#{endTime},#{status})")
        void addSchedule(Integer adminId, LocalDate date, LocalTime startTime, LocalTime endTime, TimeStatus status);

        @Insert("insert into date_status(admin_id,date) value(#{adminId},#{date})")
        void addScheduleStatus(LocalDate date, Integer adminId);

        @Update("update date_status set approval_status='approved' where admin_id=#{counselorId} and date=DATE(#{date})")
        void approveScheduleByDay(Integer counselorId, Date date);

        @Update("update schedule set approval_status='approved' where schedule_id=#{timeSlotId}")
        void approveScheduleByTimeSlot(Integer timeSlotId);

        @Select("select date,approval_status,approval_remark from date_status where date between #{startDate} and #{endDate} and "
                        +
                        "admin_id=#{adminId} order by date asc")
        @Result(property = "status", column = "approval_status", javaType = ScheduleStatus.class, typeHandler = EnumTypeHandler.class)
        List<DateAndStatusDTO> getDateAndStatusBetween(Date startDate, Date endDate, Integer adminId);

        @Select("select id,start_time,end_time,status,remark,approval_status from schedule where date=#{date} and admin_id=#{adminId} order by "
                        +
                        "start_time asc")
        @Results({ @Result(property = "status", column = "status", javaType = TimeStatus.class, typeHandler = EnumTypeHandler.class),
                        @Result(property = "approvalStatus", column = "approval_status", javaType = ScheduleStatus.class, typeHandler = EnumTypeHandler.class)

        })
        List<TimeSlot> getTimeSlotByDateAndAdminId(Date date, Integer adminId);

        @Select("select admin_id from manage where supervisor_id=#{supervisorId}")
        List<Integer> getCounselorBySupervisor(Integer supervisorId);

        @Select("select supervisor_id from manage where admin_id=#{counselorId}")
        Integer getSupervisorIdByCounselor(Integer counselorId);

        @Select("select overall_status from counselor_status where id=#{adminId}")
        @Result(property = "scheduleStatus", column = "overall_status", javaType = ScheduleStatus.class, typeHandler = EnumTypeHandler.class)
        ScheduleStatusDTO getCounselorStatusById(Integer adminId);

        @Select("select admin_id from admin where role='counselor'")
        List<Integer> getAllCounselorId();

        @Select("select account_name from admin where admin_id=#{activeCounselorId}")
        String getNameById(Integer activeCounselorId);

        @Select(("select id from counselor_status where id=#{adminId}"))
        Integer getCounselorStatus(Integer adminId);

        @Insert("insert into counselor_status(id,overall_status) value(#{adminId},#{overallStatus})")
        @Result(property = "overallStatus", column = "overall_status", javaType = ScheduleStatus.class, typeHandler = EnumTypeHandler.class)
        void addCounselorStatus(Integer adminId, ScheduleStatus overallStatus);

        @Update("update counselor_status set overall_status=#{overallStatus} where id=#{adminId}")
        @Result(property = "overallStatus", column = "overall_status", javaType = ScheduleStatus.class, typeHandler = EnumTypeHandler.class)
        void changeCounselorStatus(Integer adminId, ScheduleStatus overallStatus);

        @Select("select * from admin where role=#{role}")
        @Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class)
        List<Admin> getAllAdminByRole(Role role);

        @Select("select count(*) from manage where supervisor_id=#{supervisorId} and admin_id=#{counselorId}")
        Integer checkManageExists(Integer supervisorId, Integer counselorId);

        @Insert("insert into manage value(#{supervisorId},#{counselorId})")
        void setManage(Integer supervisorId, Integer counselorId);

        @Delete("delete from manage where supervisor_id=#{supervisorId} and admin_id=#{counselorId}")
        void removeManage(Integer supervisorId, Integer counselorId);

        @Select("select info from admin where admin_id=#{adminId}")
        String getAdminInfo(Integer adminId);

        @Update("update admin set info=#{info} where admin_id=#{adminId}")
        void updateAdminInfo(Integer adminId, String info);
}
