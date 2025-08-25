
insert into "user" values
  (0, 'admin', 'redg@yamass.de'),
  (1, 'max', 'redg@yamass.de'),
  (2, 'maria', 'redg@yamass.de');

insert into configuration values
  (0, 'is_admin', 'true'),
  (0, 'show_dashboard', 'true'),
  (0, 'confirm_all_actions', 'true');

insert into configuration values
  (1, 'is_admin', 'false'),
  (1, 'show_dashboard', 'false'),
  (1, 'confirm_all_actions', 'true');

insert into configuration values
  (2, 'is_admin', 'false'),
  (2, 'show_dashboard', 'true'),
  (2, 'confirm_all_actions', 'false');