package pl.edu.amu.wmi.dao;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Project;

import java.util.List;

@Repository
public interface ProjectDAO extends JpaRepository<Project, Long> {

    List<Project> findAllByStudyYear_StudyYear(String studyYear);

    @Query("SELECT p.id FROM Project p WHERE p.studyYear.studyYear = :studyYear")
    List<Long> findProjectIdsByStudyYear(String studyYear);

    @Query("SELECT p " +
            "FROM Project p " +
            "JOIN p.students AS s " +
            "JOIN s.userData As u " +
            "JOIN p.studyYear AS sy " +
            "WHERE u.indexNumber = :indexNumber AND sy.studyYear = :studyYear")
    Project findByProjectAdmin(String indexNumber, String studyYear);

    @Query("SELECT p AS project, pd.id AS projectDefenseId " +
            "FROM ProjectDefense pd " +
            "RIGHT JOIN pd.project p " +
            "JOIN p.studyYear sy " +
            "WHERE sy.studyYear = :studyYear AND p.acceptanceStatus = 'ACCEPTED'")
    List<Tuple> findAcceptedProjectsWithDefenseInfoForStudyYear(String studyYear);

}
