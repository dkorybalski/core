package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Supervisor;

import java.util.List;

@Repository
public interface SupervisorDAO extends JpaRepository<Supervisor, Long> {

    Supervisor findByUserData_StudyYear_StudyYearAndUserData_IndexNumber(String studyYear, String userIndex);

    Supervisor findByUserData_IndexNumber(String userIndex);

    List<Supervisor> findAllByUserData_StudyYear_StudyYearAndUserData_IndexNumberIn(String studyYear, List<String> indexNumbers);

    List<Supervisor> findAllByUserData_StudyYear_StudyYear(String studyYear);

}
