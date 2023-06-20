--STUDY_YEAR
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (10000, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'PART_TIME', 'PART_TIME#2023', '06-DPRILI0', 'LAB', '2023');

--ROLE
INSERT INTO role(id, name)
VALUES (10000, 'STUDENT');
INSERT INTO role(id, name)
VALUES (10001, 'SUPERVISOR');

--USER_DATA
INSERT INTO user_data(id, creation_date, modification_date, version, email, first_name, index_number, last_name, password, study_year)
VALUES (10000, '2023-06-20', '2023-06-20', 0, 'adanow@st.amu.edu.pl', 'Adam', '111111',  'Nowakowski', null, 'PART_TIME#2023');

--USERS_ROLES
INSERT INTO users_roles(user_data_id, role_id)
VALUES(10000, 10000);

--STUDENT
INSERT INTO student(id, creation_date, modification_date, version, is_project_admin, is_project_confirmed, pesel, project_role, project_id, user_data_id)
VALUES (10000, '2023-06-20', '2023-06-20', 0, false, false, null, null, null, 10000);