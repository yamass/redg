
create table TREE_ELEMENT (
  ID number(19) not null primary key,
  SOME_VALUE varchar2(50 CHARACTERS),
  PARENT_ID number(19) not null,

  constraint FK_TREE_ELEMENT_PARENT foreign key (PARENT_ID) REFERENCES TREE_ELEMENT(ID)
);