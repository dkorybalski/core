package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Student;

import java.util.List;

@Repository
public interface StudentDAO extends JpaRepository<Student, Long> {

    Student findByUserData_IndexNumber(String indexNumber);

    List<Student> findByUserData_StudyYear_StudyYearAndUserData_IndexNumberIn(String studyYear, List<String> indexNumbers);

    List<Student> findAllByUserData_StudyYear_StudyYear(String studyYear);

    List<Student> findAllByUserData_StudyYear_StudyYearAndUserData_IndexNumberIn(String studyYear, List<String> indexNumbers);
}
