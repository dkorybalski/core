package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.ProjectDefense;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectDefenseDAO extends JpaRepository<ProjectDefense, Long> {

    @EntityGraph(attributePaths = {"supervisorDefenseAssignments", "supervisorDefenseAssignments.defenseTimeSlot"})
    List<ProjectDefense> findAllByStudyYearAndSupervisorDefenseAssignmentsNotEmpty(String studyYear);

    List<ProjectDefense> findAllByProjectId(Long projectId);

    ProjectDefense findByProjectId(Long projectId);

    @Query("SELECT pd " +
            "FROM ProjectDefense pd " +
            "JOIN pd.supervisorDefenseAssignments sda " +
            "JOIN sda.defenseTimeSlot dts " +
            "WHERE dts.date = :date AND pd.project IS NOT NULL")
    List<ProjectDefense> findAllByDefenseDateAndProjectNotNull(LocalDate date);
}
