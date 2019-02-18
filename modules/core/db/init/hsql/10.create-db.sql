-- begin LEAFLETDEMO_SALESPERSON
create table LEAFLETDEMO_SALESPERSON (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FIRST_NAME varchar(255) not null,
    PHONE varchar(255) not null,
    PHOTO_ID varchar(36),
    LATITUDE double not null,
    LONGITUDE double not null,
    --
    primary key (ID)
)^
-- end LEAFLETDEMO_SALESPERSON
