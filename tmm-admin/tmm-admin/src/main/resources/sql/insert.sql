INSERT INTO `t_role` VALUES ('1', 'admin', '2018-06-01 16:24:56', '2018-06-01 16:25:02') on DUPLICATE key update update_time = NOW();
INSERT INTO `t_role` VALUES ('2', '普通角色', '2018-06-07 11:53:02', '2018-06-07 11:53:05') on DUPLICATE key update update_time = NOW();
INSERT INTO t_role_user(pid,role_id,user_id) VALUES (1, 1, 1) on DUPLICATE key update pid =VALUES(pid),role_id=VALUES(role_id),user_id=VALUES(user_id);
INSERT INTO `t_user` VALUES ('1', 'admin', 'c3284d0f94606de1fd2af172aba15bf3', '1', '2018-06-05 13:59:24', '2018-06-06 14:19:27') on DUPLICATE key update update_time = NOW();
