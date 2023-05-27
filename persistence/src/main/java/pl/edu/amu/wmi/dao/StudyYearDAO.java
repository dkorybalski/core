package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.StudyYear;

@Repository
public interface StudyYearDAO extends JpaRepository<StudyYear, Long> {

    StudyYear findByStudyYear(String studyYear);

}
