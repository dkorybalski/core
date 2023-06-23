package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.StudentProject;

public interface StudentProjectDAO extends JpaRepository<StudentProject, Long> {

    StudentProject findByStudent_IdAndProject_Id(Long studentId, Long projectId);

}
