package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.StudyYear;

import java.util.List;

@Repository
public interface ProjectDAO extends JpaRepository<Project, Long> {

    List<Project> findAllByStudyYear(StudyYear studyYear);

}
