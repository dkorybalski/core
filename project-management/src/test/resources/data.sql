--STUDY_YEAR
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (1, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'FULL_TIME', 'FULL_TIME#2023', '06-DPRILI0', 'LAB', '2023');

--ROLE
INSERT INTO public.role(id, name) VALUES (1, 'STUDENT');
INSERT INTO public.role(id, name) VALUES (2, 'PROJECT_ADMIN');
INSERT INTO public.role(id, name) VALUES (3, 'SUPERVISOR');
INSERT INTO public.role(id, name) VALUES (4, 'COORDINATOR');


INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (1, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Coordinator 1', 'Coordinator 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (2, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Supervisor 1', 'Supervisor 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (3, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Supervisor 2', 'Supervisor 2');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (4, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 1', 'Student 1');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (5, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 2', 'Student 2');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (6, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 3', 'Student 3');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (7, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 4', 'Student 4');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (8, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 5', 'Student 5');
INSERT INTO public.user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name) VALUES (9, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'test-email@gmail.com', 'Test', 'Student 6', 'Student 6');

--
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (1, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 1, 3, 'FULL_TIME#2023', 1);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (2, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 2, 3, 'FULL_TIME#2023', 2);
INSERT INTO public.supervisor(id, creation_date, modification_date, version, group_number, max_number_of_projects, study_year, user_data_id) VALUES (3, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 3, 0, 'FULL_TIME#2023', 3);
--

INSERT INTO public.project(id, creation_date, modification_date, version, acceptance_status, description, name, technologies, study_year, supervisor_id)
VALUES (1, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, 'ACCEPTED', 'test', 'test', '{java}', 'FULL_TIME#2023', 1);

INSERT INTO public.evaluation_card_template (id, creation_date,modification_date,version,min_points_threshold_first_semester,min_points_threshold_second_semester,study_year) VALUES
(1001, '2023-12-30 21:19:19.73432','2023-12-30 21:19:19.73432',0,0.8,0.7,'FULL_TIME#2023');

INSERT INTO public.evaluation_card (id, creation_date,is_approved_for_defense,is_disqualified, is_active,modification_date,version,evaluation_card_template_id,project_id,evaluation_phase,evaluation_status,final_grade,semester,total_points) VALUES
(1001,'2023-12-30 21:20:17.864204',false,true, true,'2023-12-30 21:20:17.896515',0,1001,1,'SEMESTER_PHASE','ACTIVE',NULL,'FIRST',0.0);


INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (1, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, true, true, NULL, 'BACKEND', 1, 4, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (2, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 5, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (3, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 6, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (4, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 7, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (5, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 8, 'FULL_TIME#2023');
INSERT INTO public.student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id, study_year) VALUES (6, '2023-12-17 20:22:31.687', '2023-12-17 20:22:31.687', 0, false, false, NULL, NULL, NULL, 9, 'FULL_TIME#2023');

INSERT INTO public.users_roles(user_data_id, role_id) VALUES (1, 4);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (1, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (2, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (3, 3);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (4, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (5, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (6, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (7, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (8, 1);
INSERT INTO public.users_roles(user_data_id, role_id) VALUES (9, 1);


INSERT INTO public.student_project(is_project_admin, is_project_confirmed, project_role, project_id, student_id)
VALUES (true, true, 'BACKEND', 1, 1);
