create database if not exists wx_ai default character set utf8mb4 collate utf8mb4_unicode_ci;
use wx_ai;

create table if not exists sys_user (
    id          bigint auto_increment primary key,
    username    varchar(32)  not null comment '用户名',
    password    varchar(64)  not null comment '密码（SHA-256）',
    nickname    varchar(32)  null     comment '昵称',
    token       varchar(64)  null     comment '登录令牌',
    created_at  timestamp    default current_timestamp,
    updated_at  timestamp    default current_timestamp on update current_timestamp,
    unique key uk_username (username)
) engine=innodb default charset=utf8mb4 comment='用户表';

create table if not exists ai_chat_memory (
    id          bigint auto_increment primary key,
    user_id     varchar(64)  not null comment '用户id',
    memory_id   varchar(64)  not null comment '记忆id（会话id）',
    title       varchar(100) null     comment '会话标题',
    messages    text         null     comment 'AI记忆消息json（最近N条）',
    created_at  timestamp    default current_timestamp,
    updated_at  timestamp    default current_timestamp on update current_timestamp,
    unique key uk_memory_id (memory_id),
    index idx_user_id (user_id)
) engine=innodb default charset=utf8mb4 comment='ai对话记忆表';

create table if not exists ai_chat_message (
    id          bigint auto_increment primary key,
    user_id     varchar(64)  not null comment '用户id',
    memory_id   varchar(64)  not null comment '会话id',
    role        varchar(10)  not null comment 'USER/AI',
    content     text         not null comment '消息内容',
    created_at  timestamp    default current_timestamp,
    index idx_memory_page (memory_id, id desc),
    index idx_user_id (user_id)
) engine=innodb default charset=utf8mb4 comment='对话消息明细表';