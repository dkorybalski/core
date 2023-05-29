package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Student;

@Repository
public interface StudentDAO extends JpaRepository<Student, Long> {

    Student findByUserData_IndexNumber(String indexNumber);

//    Student findByUserData_IndexNumberAndUserData_StudyYear_StudyYear(String indexNumber);

}
