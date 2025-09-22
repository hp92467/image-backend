create database  if not exists image;

use image;


create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- picture（图片表），根据需求可以做出如下 SQL 设计：

create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                      null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId)
) comment '图片' collate = utf8mb4_unicode_ci;

ALTER TABLE picture

    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';


CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

ALTER TABLE picture

    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';
-- 空间业务表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    index idx_userId (userId),
    index idx_spaceName (spaceName),
    index idx_spaceLevel (spaceLevel)
) comment '空间' collate = utf8mb4_unicode_ci;

ALTER TABLE picture
    ADD COLUMN spaceId  bigint  null comment '空间 id（为空表示公共空间）';


CREATE INDEX idx_spaceId ON picture (spaceId);

ALTER TABLE picture ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 为 space 表添加空间类型字段
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型: 0-私有空间, 1-团队空间';

-- 为新字段创建索引以提高查询性能
CREATE INDEX idx_spaceType ON space (spaceType);

-- 创建空间用户关联表 (space_user)
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                             not null comment '空间 id',
    userId     bigint                             not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'      null comment '空间角色',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',

    -- 联合唯一索引，确保一个用户在一个空间里只有一个角色
    UNIQUE KEY uk_spaceId_userId (spaceId, userId),
    -- 普通索引，提高查询效率
    INDEX idx_spaceId (spaceId),
    INDEX idx_userId (userId)
) comment '空间用户关联' collate = utf8mb4_unicode_ci;
