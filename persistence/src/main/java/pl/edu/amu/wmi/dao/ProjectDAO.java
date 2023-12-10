package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Project;

import java.util.List;

@Repository
public interface ProjectDAO extends JpaRepository<Project, Long> {

    List<Project> findAllByStudyYear_StudyYear(String studyYear);

    @Query("SELECT p " +
            "FROM Project p " +
            "JOIN p.students AS s " +
            "JOIN s.userData As u " +
            "JOIN p.studyYear AS st " +
            "WHERE u.indexNumber = :indexNumber AND st.studyYear = :studyYear")
    Project findByProjectAdmin(String indexNumber, String studyYear);

}
