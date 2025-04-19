package com.mygo.vo;

import com.mygo.enumeration.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminSessionVO {

    private String id;

    private String clientId;

    private String clientName;

    private String clientAvatar;

    private String counselorId;

    private String counselorName;

    private String counselorAvatar;

    private LocalDateTime lastMessageTime;

    private String lastMessage;

    private int unreadCount;

    private UserStatus status;

}
