    create table hibernate_sequence (
       next_val bigint
    ) engine=InnoDB;

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    insert into hibernate_sequence values ( 1 );

    create table supervisor_unit (
       unit_id bigint,
        supervisor_id bigint not null,
        primary key (supervisor_id)
    ) engine=InnoDB;

    create table ticket_technicians (
       ticket_id bigint not null,
        technician_id bigint not null
    ) engine=InnoDB;

    create table tickets (
       id bigint not null,
        priority int default 0,
        progress int default 0,
        dateAssigned datetime,
        dateUpdated datetime,
        details varchar(3000) not null,
        endDate datetime,
        startDate datetime not null,
        subject varchar(255) not null,
        created_for_name varchar(255),
        created_for_email varchar(255) not null,
        created_for_phone varchar(255),
        created_for_department  varchar(255),
        location varchar(255),
        userid bigint,
        unitid bigint,
        primary key (id)
    ) engine=InnoDB;

   
     create table units (
       id bigint not null,
        description varchar(255),
        email varchar(255) not null,
        location varchar(255) not null,
        name varchar(255) not null,
        phone varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table updates (
       id bigint not null,
        modifiedDate datetime not null,
        updateDetails varchar(255) not null,
        modifier bigint,
        ticketid bigint,
        primary key (id)
    ) engine=InnoDB;

    create table users (
       id bigint not null,
        department varchar(255),
        email varchar(255) not null,
        firstName varchar(255) not null,
        hash varchar(255) not null,
        lastName varchar(255) not null,
        phoneNumber varchar(255) not null,
        position int default 2,
        username varchar(255) not null,
        unitid bigint,
        primary key (id)
    ) engine=InnoDB;

    alter table users 
       add constraint UK_6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table users 
       add constraint UK_r43af9ap4edm43mmtq01oddj6 unique (username);

    alter table supervisor_unit 
       add constraint FKjlkmskidh9mke243ddqcbjrsp 
       foreign key (unit_id) 
       references units (id);

    alter table supervisor_unit 
       add constraint FKqiehylcg40k4q8s8167nliyq7 
       foreign key (supervisor_id) 
       references users (id);

    alter table ticket_technicians 
       add constraint FKkvckku7hvsd59vfejvy2rho2s 
       foreign key (technician_id) 
       references users (id);

    alter table ticket_technicians 
       add constraint FKawo4w3hcl79i748u1d9xn0ye7 
       foreign key (ticket_id) 
       references tickets (id);

    alter table tickets 
       add constraint FKgm2k38en2feysx9g8p33w0fqn 
       foreign key (userid) 
       references users (id);

    alter table tickets 
       add constraint FKijrn0649tl8swlbvletghwy98 
       foreign key (unitid) 
       references units (id);

    alter table updates 
       add constraint FKkm5vbuo5qarrdod33upfsgugn 
       foreign key (modifier) 
       references users (id);

    alter table updates 
       add constraint FK7nknh8sye6txi4k0jdf9ue0hd 
       foreign key (ticketid) 
       references tickets (id);

    alter table users 
       add constraint FKsp65j502ysjigut2jnnst7p0d 
       foreign key (unitid) 
       references units (id);
       
 
