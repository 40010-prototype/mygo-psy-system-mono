package com.mygo.mapper;

import com.mygo.dto.DateAndStatusDTO;
import com.mygo.dto.StartAndEndTime;
import com.mygo.entity.Admin;
import com.mygo.entity.Consult;
import com.mygo.entity.Message;
import com.mygo.entity.TimeSlot;
import com.mygo.enumeration.*;
import com.mygo.handler.EnumTypeHandler;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE account_name=#{accountName}")
    @Results({@Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class)
            , @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)})
    Admin getAdminByName(String accountName);

    @Select("SELECT * FROM admin WHERE admin_id=#{id}")
    @Results({@Result(property = "role", column = "role", javaType = Role.class, typeHandler = EnumTypeHandler.class)
            , @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)})
    Admin getAdminById(Integer id);

    @Insert("INSERT INTO admin(admin_id, account_name, real_name, email, password,role,info) VALUES(#{id}, " +
            "#{accountName},#{realName},  #{email},#{password}, #{role},#{info})")
    void addAdmin(Integer id, String accountName, String realName, String email, String password,
                  Role role, String info);

    @Select("SELECT email FROM admin WHERE account_name=#{accountName}")
    String getEmailByAccountName(String accountName);

    @Update("UPDATE admin SET password=#{password} WHERE name=#{name}")
    void updatePassword(String name, String password);

    @Select("select consult_id,user_id from consult_record where admin_id=#{adminId}")
    @Results({
            @Result(property = "status", column = "status", javaType = ConsultStatus.class, typeHandler =
                    EnumTypeHandler.class)
    })
    List<Consult> getConsultInfoByAdminId(Integer adminId);

    @Select("select * from message where consult_id=#{sessionId} order by time desc  ")
    @Results({
            @Result(property = "status", column = "status", javaType = MessageStatus.class, typeHandler =
                    EnumTypeHandler.class),
            @Result(property = "messageType", column = "message_type", javaType = MessageType.class, typeHandler =
                    EnumTypeHandler.class),
            @Result(property = "sender", column = "sender", javaType = Sender.class, typeHandler =
                    EnumTypeHandler.class),
            @Result(property = "time", column = "time", javaType = LocalDateTime.class)
    })
    List<Message> getHistoryMessageBySessionId(Integer sessionId, Integer offset, Integer limit);

    @Update("update message set status='read' where consult_id=#{id}")
    void setRead(Integer id);

    @Select("select start_time,end_time from schedule where date=#{date} and admin_id=#{adminId}")
    List<StartAndEndTime> getTimePeriodByDateAndAdminId(Date date, Integer adminId);

    @Insert("insert into schedule(admin_id,date,start_time,end_time,status) value (#{adminId},#{date},#{startTime}," +
            "#{endTime},#{status})")
    void addSchedule(Integer adminId, Date date, LocalTime startTime, LocalTime endTime, TimeStatus status);

    @Insert("insert into date_status(admin_id,date) value(#{adminId},#{date})")
    void addScheduleStatus(Date date, Integer adminId);

    @Update("update date_status set approval_status='approved' where schedule_id=#{scheduleId}")
    void approveScheduleByDay(Integer scheduleId);

    @Update("update schedule set approval_status='approved' where schedule_id=#{timeSlotId}")
    void approveScheduleByTimeSlot(Integer timeSlotId);

    @Select("select date,approval_status from date_status where date between #{startDate} and #{endDate} and " +
            "admin_id=#{adminId} order by date asc")
    @Result(property = "status", column = "approval_status", javaType = ScheduleStatus.class, typeHandler =
            EnumTypeHandler.class)
    List<DateAndStatusDTO> getDateAndStatusBetween(Date startDate, Date endDate, Integer adminId);

    @Select("select id,start_time,end_time,status from schedule where date=#{date} and admin_id={adminId} order by " +
            "start_time asc")
    @Result(property = "status", column = "status", javaType = ScheduleStatus.class, typeHandler =
            EnumTypeHandler.class)
    List<TimeSlot> getTimeSlotByDateAndAdminId(Date date, Integer adminId);

    @Select("select counselor_id from manage where supervisor_id=#{supervisorId}")
    List<Integer> getCounselorBySupervisor(Integer supervisorId);

    @Select("select overall_status from counselor_status where id=#{adminId}")
    @Result(column = "overall_status",javaType = ScheduleStatus.class,typeHandler=EnumTypeHandler.class)
    ScheduleStatus getCounselorStatusById(Integer adminId);

    @Select("select admin_id from admin where role='counselor'")
    List<Integer> getAllCounselor();

}
