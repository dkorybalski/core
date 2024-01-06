--STUDY_YEAR
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (1, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'FULL_TIME', 'FULL_TIME#2023', '06-DPRILI0', 'LAB', '2023');

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

--SUPERVISOR
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (10, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 1, 3, 'FULL_TIME#2023', 10);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (20, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 2, 3, 'FULL_TIME#2023', 20);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (30, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 3, 0, 'FULL_TIME#2023', 30);

--STUDENT
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (10, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 40, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (20, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 50, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (30, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 60, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (40, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 70, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (50, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 80, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (60, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 90, 'FULL_TIME#2023');

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
