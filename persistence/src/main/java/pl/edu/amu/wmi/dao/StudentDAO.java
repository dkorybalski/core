package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Student;

import java.util.List;

@Repository
public interface StudentDAO extends JpaRepository<Student, Long> {

    Student findByUserData_IndexNumber(String indexNumber);

    List<Student> findByStudyYearAndUserData_IndexNumberIn(String studyYear, List<String> indexNumbers);

    List<Student> findAllByStudyYear(String studyYear);

    List<Student> findAllByStudyYear_AndUserData_IndexNumberIn(String studyYear, List<String> indexNumbers);

    List<Student> findAllByUserData_IndexNumber(String indexNumber);
}
