-- begin LEAFLETDEMO_SALESPERSON
alter table LEAFLETDEMO_SALESPERSON add constraint FK_LEAFLETDEMO_SALESPERSON_ON_PHOTO foreign key (PHOTO_ID) references SYS_FILE(ID)^
create index IDX_LEAFLETDEMO_SALESPERSON_ON_PHOTO on LEAFLETDEMO_SALESPERSON (PHOTO_ID)^
-- end LEAFLETDEMO_SALESPERSON