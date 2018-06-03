
    create table hibernate_sequence (
       next_val bigint
    ) engine=InnoDB;

    insert into hibernate_sequence values ( 1 );

    create table users (
       id bigint not null,
        enabled bit not null,
        password varchar(255) not null,
        username varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    alter table users 
       add constraint UK_r43af9ap4edm43mmtq01oddj6 unique (username);
