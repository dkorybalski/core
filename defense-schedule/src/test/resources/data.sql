--STUDY_YEAR
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (1, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'FULL_TIME', 'FULL_TIME#2023', '06-DPRILI0', 'LAB', '2023');
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (2, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'PART_TIME', 'PART_TIME#2023', '06-DPRILI0', 'LAB', '2023');

--ROLE
INSERT INTO public.role(id, name) VALUES (1, 'STUDENT');
INSERT INTO public.role(id, name) VALUES (2, 'PROJECT_ADMIN');
INSERT INTO public.role(id, name) VALUES (3, 'SUPERVISOR');
INSERT INTO public.role(id, name) VALUES (4, 'COORDINATOR');

--USER_DATA
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (10, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Coordinator 1', 'Coordinator 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (20, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Supervisor 1', 'Supervisor 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (30, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Supervisor 2', 'Supervisor 2');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (40, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 1', 'Student 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (50, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 2', 'Student 2');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (60, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 3', 'Student 3');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (70, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 4', 'Student 4');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (80, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 5', 'Student 5');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (90, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 6', 'Student 6');

--SUPERVISOR FULL_TIME#2023
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (10, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 1, 3, 'FULL_TIME#2023', 10);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (20, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 2, 3, 'FULL_TIME#2023', 20);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (30, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 3, 0, 'FULL_TIME#2023', 30);

--SUPERVISOR PART_TIME#2023
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (100, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 1, 3, 'PART_TIME#2023', 10);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (200, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 2, 3, 'PART_TIME#2023', 20);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (300, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 3, 0, 'PART_TIME#2023', 30);

--PROJECT FULL_TIME#2023
INSERT INTO public.project(id, creation_date, modification_date, version, acceptance_status, description, name, technologies, study_year, supervisor_id)
VALUES (1001, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'ACCEPTED', 'test1', 'Project Test 1', '{java}', 'FULL_TIME#2023', 10);
INSERT INTO public.project(id, creation_date, modification_date, version, acceptance_status, description, name, technologies, study_year, supervisor_id)
VALUES (1002, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'ACCEPTED', 'test2', 'Project Test 2', '{python}', 'FULL_TIME#2023', 20);

--STUDENT FULL_TIME#2023
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (10, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, true, true, NULL, 'BACKEND', 1001, 40, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (20, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, true, NULL, 'FRONTEND', 1001, 50, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (30, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, true, true, NULL, 'BACKEND', 1002, 60, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (40, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 70, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (50, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 80, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (60, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 90, 'FULL_TIME#2023');

--STUDENT_PROJECT FULL_TIME#2023
INSERT INTO public.student_project(is_project_admin, is_project_confirmed, project_role, project_id, student_id) VALUES
(true, true, 'BACKEND', 1001, 10),
(false, true, 'FRONTEND', 1001, 20),
(true, true, 'BACKEND', 1002, 30);

--USER_ROLES
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (10, 4);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (10, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (20, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (30, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (40, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (50, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (60, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (70, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (80, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (90, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (40, 2);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (60, 2);

--DEFENSE_SCHEDULE_CONFIG FULL_TIME#2023
INSERT INTO public.defense_schedule_config (id, creation_date,modification_date,version,defense_duration,defense_phase,end_date,end_time,start_date,start_time,study_year,is_active) VALUES
    (100, '2024-01-01 19:35:26.959797','2024-01-01 19:35:26.959797',0,30,'DEFENSE_PROJECT_REGISTRATION','2024-01-09','12:00:00','2024-01-08','08:00:00','FULL_TIME#2023',true);

--DEFENSE_TIME_SLOT FULL_TIME#2023
INSERT INTO public.defense_time_slot (id,creation_date,modification_date,version,date,duration,end_time,start_time,study_year,defense_schedule_config_id) VALUES
(1001, '2024-01-01 19:35:26.971876','2024-01-01 19:35:26.971876',0,'2024-01-08',30,'08:30:00','08:00:00','FULL_TIME#2023',100),
(1002, '2024-01-01 19:35:26.974879','2024-01-01 19:35:26.974879',0,'2024-01-08',30,'09:00:00','08:30:00','FULL_TIME#2023',100),
(1003,'2024-01-01 19:35:26.975876','2024-01-01 19:35:26.975876',0,'2024-01-08',30,'09:30:00','09:00:00','FULL_TIME#2023',100),
(1004,'2024-01-01 19:35:26.976876','2024-01-01 19:35:26.976876',0,'2024-01-08',30,'10:00:00','09:30:00','FULL_TIME#2023',100),
(1005,'2024-01-01 19:35:26.977875','2024-01-01 19:35:26.977875',0,'2024-01-08',30,'10:30:00','10:00:00','FULL_TIME#2023',100),
(1006,'2024-01-01 19:35:26.98088','2024-01-01 19:35:26.98088',0,'2024-01-08',30,'11:00:00','10:30:00','FULL_TIME#2023',100),
(1007,'2024-01-01 19:35:26.981877','2024-01-01 19:35:26.981877',0,'2024-01-08',30,'11:30:00','11:00:00','FULL_TIME#2023',100),
(1008,'2024-01-01 19:35:26.983491','2024-01-01 19:35:26.983491',0,'2024-01-08',30,'12:00:00','11:30:00','FULL_TIME#2023',100),
(1009,'2024-01-01 19:35:26.985399','2024-01-01 19:35:26.985399',0,'2024-01-09',30,'08:30:00','08:00:00','FULL_TIME#2023',100),
(1010,'2024-01-01 19:35:26.9874','2024-01-01 19:35:26.9874',0,'2024-01-09',30,'09:00:00','08:30:00','FULL_TIME#2023',100),
(1011,'2024-01-01 19:35:26.9884','2024-01-01 19:35:26.9884',0,'2024-01-09',30,'09:30:00','09:00:00','FULL_TIME#2023',100),
(1012,'2024-01-01 19:35:26.989825','2024-01-01 19:35:26.989825',0,'2024-01-09',30,'10:00:00','09:30:00','FULL_TIME#2023',100),
(1013,'2024-01-01 19:35:26.991832','2024-01-01 19:35:26.991832',0,'2024-01-09',30,'10:30:00','10:00:00','FULL_TIME#2023',100),
(1014,'2024-01-01 19:35:26.993834','2024-01-01 19:35:26.993834',0,'2024-01-09',30,'11:00:00','10:30:00','FULL_TIME#2023',100),
(1015,'2024-01-01 19:35:26.994833','2024-01-01 19:35:26.994833',0,'2024-01-09',30,'11:30:00','11:00:00','FULL_TIME#2023',100),
(1016,'2024-01-01 19:35:26.997351','2024-01-01 19:35:26.997351',0,'2024-01-09',30,'12:00:00','11:30:00','FULL_TIME#2023',100);

--PROJECT_DEFENSE FULL_TIME#2023
INSERT INTO public.project_defense (id,creation_date,modification_date,version,study_year,project_id) VALUES
(1001,'2024-01-01 19:36:11.715232','2024-01-01 19:36:11.715232',0,'FULL_TIME#2023',NULL),
(1002,'2024-01-01 19:36:11.723959','2024-01-01 19:36:11.723959',0,'FULL_TIME#2023',NULL),
(1003,'2024-01-01 19:36:28.261369','2024-01-01 19:36:28.261369',0,'FULL_TIME#2023',NULL),
(1004,'2024-01-01 19:36:28.267911','2024-01-01 19:36:28.267911',0,'FULL_TIME#2023',NULL);

--SUPERVISOR_DEFENSE_ASSIGNMENT FULL_TIME#2023 SUPERVISOR 10
INSERT INTO public.supervisor_defense_assignment (id,creation_date,modification_date,version,classroom,committee_identifier,is_available,is_chairperson,defense_time_slot_id,project_defense_id,supervisor_id) VALUES
(1000,'2024-01-01 19:35:27.025866','2024-01-01 19:35:27.025866',0,NULL,NULL,false,false,1001,NULL,10),
(1001,'2024-01-01 19:35:27.02887','2024-01-01 19:36:11.725813',2,NULL,'A',false,true,1002,1002,10),
(1002,'2024-01-01 19:35:27.029873','2024-01-01 19:36:11.719236',2,NULL,'A',false,true,1003,1001,10),
(1003,'2024-01-01 19:35:27.030905','2024-01-01 19:35:27.030905',0,NULL,NULL,false,false,1004,NULL,10),
(1004,'2024-01-01 19:35:27.031768','2024-01-01 19:35:27.031768',0,NULL,NULL,false,false,1005,NULL,10),
(1005,'2024-01-01 19:35:27.032875','2024-01-01 19:35:27.032875',0,NULL,NULL,false,false,1006,NULL,10),
(1006,'2024-01-01 19:35:27.034846','2024-01-01 19:35:27.034846',0,NULL,NULL,false,false,1007,NULL,10),
(1007,'2024-01-01 19:35:27.034846','2024-01-01 19:35:27.034846',0,NULL,NULL,false,false,1008,NULL,10),
(1008,'2024-01-01 19:35:27.037547','2024-01-01 19:35:27.037547',0,NULL,NULL,false,false,1009,NULL,10),
(1009,'2024-01-01 19:35:27.039235','2024-01-01 19:35:27.039235',0,NULL,NULL,false,false,1010,NULL,10),
(1010,'2024-01-01 19:35:27.040305','2024-01-01 19:35:27.040305',0,NULL,NULL,false,false,1011,NULL,10),
(1011,'2024-01-01 19:35:27.042241','2024-01-01 19:35:27.042241',0,NULL,NULL,false,false,1012,NULL,10),
(1012,'2024-01-01 19:35:27.044243','2024-01-01 19:36:28.268511',3,NULL,'B',false,true,1013,1004,10),
(1013,'2024-01-01 19:35:27.045263','2024-01-01 19:36:28.263875',3,NULL,'B',false,true,1014,1003,10),
(1014,'2024-01-01 19:35:27.047263','2024-01-01 19:35:27.047263',0,NULL,NULL,false,false,1015,NULL,10),
(1015,'2024-01-01 19:35:27.048316','2024-01-01 19:35:27.048316',0,NULL,NULL,false,false,1016,NULL,10);

--SUPERVISOR_DEFENSE_ASSIGNMENT FULL_TIME#2023 SUPERVISOR 20
INSERT INTO public.supervisor_defense_assignment (id,creation_date,modification_date,version,classroom,committee_identifier,is_available,is_chairperson,defense_time_slot_id,project_defense_id,supervisor_id) VALUES
(1016,'2024-01-01 19:35:27.050351','2024-01-01 19:35:27.050351',0,NULL,NULL,false,false,1001,NULL,20),
(1017,'2024-01-01 19:35:27.052134','2024-01-01 19:36:11.727815',2,NULL,'A',false,false,1002,1002,20),
(1018,'2024-01-01 19:35:27.053788','2024-01-01 19:36:11.721813',2,NULL,'A',false,false,1003,1001,20),
(1019,'2024-01-01 19:35:27.055052','2024-01-01 19:35:27.055052',0,NULL,NULL,false,false,1004,NULL,20),
(1020,'2024-01-01 19:35:27.056059','2024-01-01 19:35:27.056059',0,NULL,NULL,false,false,1005,NULL,20),
(1021,'2024-01-01 19:35:27.058395','2024-01-01 19:35:27.058395',0,NULL,NULL,false,false,1006,NULL,20),
(1022,'2024-01-01 19:35:27.059445','2024-01-01 19:35:27.059445',0,NULL,NULL,false,false,1007,NULL,20),
(1023,'2024-01-01 19:35:27.060454','2024-01-01 19:35:27.060454',0,NULL,NULL,false,false,1008,NULL,20),
(1024,'2024-01-01 19:35:27.061528','2024-01-01 19:35:27.061528',0,NULL,NULL,false,false,1009,NULL,20),
(1025,'2024-01-01 19:35:27.063583','2024-01-01 19:35:27.063583',0,NULL,NULL,false,false,1010,NULL,20),
(1026,'2024-01-01 19:35:27.064597','2024-01-01 19:35:27.064597',0,NULL,NULL,false,false,1011,NULL,20),
(1027,'2024-01-01 19:35:27.067194','2024-01-01 19:35:27.067194',0,NULL,NULL,false,false,1012,NULL,20),
(1028,'2024-01-01 19:35:27.068713','2024-01-01 19:36:28.269521',3,NULL,'B',false,false,1013,1004,20),
(1029,'2024-01-01 19:35:27.069786','2024-01-01 19:36:28.26488',3,NULL,'B',false,false,1014,1003,20),
(1030,'2024-01-01 19:35:27.069786','2024-01-01 19:35:27.069786',0,NULL,NULL,false,false,1015,NULL,20),
(1031,'2024-01-01 19:35:27.070819','2024-01-01 19:35:27.070819',0,NULL,NULL,false,false,1016,NULL,20);

--SUPERVISOR_DEFENSE_ASSIGNMENT FULL_TIME#2023 SUPERVISOR 30
INSERT INTO public.supervisor_defense_assignment (id,creation_date,modification_date,version,classroom,committee_identifier,is_available,is_chairperson,defense_time_slot_id,project_defense_id,supervisor_id) VALUES
(1032,'2024-01-01 19:35:27.071898','2024-01-01 19:35:27.071898',0,NULL,NULL,false,false,1001,NULL,30),
(1033,'2024-01-01 19:35:27.072937','2024-01-01 19:36:11.726909',2,NULL,'A',false,false,1002,1002,30),
(1034,'2024-01-01 19:35:27.072937','2024-01-01 19:36:11.720804',2,NULL,'A',false,false,1003,1001,30),
(1035,'2024-01-01 19:35:27.073916','2024-01-01 19:35:27.073916',0,NULL,NULL,false,false,1004,NULL,30),
(1036,'2024-01-01 19:35:27.074917','2024-01-01 19:35:27.074917',0,NULL,NULL,false,false,1005,NULL,30),
(1037,'2024-01-01 19:35:27.076425','2024-01-01 19:35:27.076425',0,NULL,NULL,false,false,1006,NULL,30),
(1038,'2024-01-01 19:35:27.078472','2024-01-01 19:35:27.078472',0,NULL,NULL,false,false,1007,NULL,30),
(1039,'2024-01-01 19:35:27.079544','2024-01-01 19:35:27.079544',0,NULL,NULL,false,false,1008,NULL,30),
(1040,'2024-01-01 19:35:27.079544','2024-01-01 19:35:27.079544',0,NULL,NULL,false,false,1009,NULL,30),
(1041,'2024-01-01 19:35:27.08054','2024-01-01 19:35:27.08054',0,NULL,NULL,false,false,1010,NULL,30),
(1042,'2024-01-01 19:35:27.081494','2024-01-01 19:35:27.081494',0,NULL,NULL,false,false,1011,NULL,30),
(1043,'2024-01-01 19:35:27.08257','2024-01-01 19:35:27.08257',0,NULL,NULL,false,false,1012,NULL,30),
(1044,'2024-01-01 19:35:27.083537','2024-01-01 19:35:27.083537',0,NULL,NULL,false,false,1013,NULL,30),
(1045,'2024-01-01 19:35:27.084529','2024-01-01 19:35:27.084529',0,NULL,NULL,false,false,1014,NULL,30),
(1046,'2024-01-01 19:35:27.085432','2024-01-01 19:35:27.085432',0,NULL,NULL,false,false,1015,NULL,30),
(1047,'2024-01-01 19:35:27.086549','2024-01-01 19:35:27.086549',0,NULL,NULL,false,false,1016,NULL,30);
