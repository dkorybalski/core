--STUDY_YEAR
INSERT INTO study_year(id, creation_date, modification_date, version, first_semester_code, is_active, second_semester_code, study_type, study_year, subject_code, subject_type, "year")
VALUES (1, '2023-05-28', '2023-05-28', 0, '2023/SL', 'true', '2023/SZ', 'PART_TIME', 'PART_TIME#2023', '06-DPRILI0', 'LAB', '2023');

--ROLE
INSERT INTO public.role(id, name) VALUES (1, 'STUDENT');
INSERT INTO public.role(id, name) VALUES (2, 'PROJECT_ADMIN');
INSERT INTO public.role(id, name) VALUES (3, 'SUPERVISOR');
INSERT INTO public.role(id, name) VALUES (4, 'COORDINATOR');
