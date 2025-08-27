-- as with all test SQL files, the schema created here does not confirm to best practices in database design
-- it simply uses somewhat relatable scenarios and creates schemas that test certain features of RedG


create table RESTAURANT (
  ID number(19) not null primary key,
  NAME varchar2(50 CHARACTERS) not null
);

create table GUEST (
  ID number(19) not null primary key,
  FIRST_NAME varchar2(50 CHARACTERS),
  LAST_NAME varchar2(50 CHARACTERS) not null
);

create table RESERVATION (
  RESTAURANT_ID number(19) not null,
  GUEST_ID number(19) not null,
  TIME TIMESTAMP not null,

  primary key (RESTAURANT_ID, GUEST_ID, TIME),
  constraint FK_RESERVATION_RESTAURANT foreign key (RESTAURANT_ID) references RESTAURANT(ID),
  constraint FK_RESERVATION_GUEST foreign key (GUEST_ID) references GUEST(ID)
);

create table WAITER (
  ID number(19) not null primary key,
  NAME varchar2(50 CHARACTERS) not null,
  RESTAURANT number(19),

  constraint FK_WAITER_WORKS_AT foreign key (RESTAURANT) references RESTAURANT(ID)
);

create table WAITER_RESERVATION (
  WAITER number(19) not null,
  RESTAURANT_ID number(19) not null,
  GUEST_ID number(19) not null,
  TIME TIMESTAMP not null,

  constraint FK_W_R_WAITER foreign key (WAITER) references WAITER(ID),
  constraint FK_W_R_RESERVATION foreign key (RESTAURANT_ID, GUEST_ID, TIME) references RESERVATION(RESTAURANT_ID, GUEST_ID, "TIME")
);