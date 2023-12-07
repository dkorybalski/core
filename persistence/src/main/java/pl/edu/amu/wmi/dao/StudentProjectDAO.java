package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.StudentProject;

@Repository
public interface StudentProjectDAO extends JpaRepository<StudentProject, Long> {

    StudentProject findByStudent_IdAndProject_Id(Long studentId, Long projectId);

}
