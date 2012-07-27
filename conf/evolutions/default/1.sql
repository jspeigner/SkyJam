# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table users (
  id                        integer auto_increment not null,
  username                  varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  registered_date           date,
  last_login_date           date,
  type                      varchar(255),
  image_storage_object_id   integer,
  constraint pk_users primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table users;

SET FOREIGN_KEY_CHECKS=1;

