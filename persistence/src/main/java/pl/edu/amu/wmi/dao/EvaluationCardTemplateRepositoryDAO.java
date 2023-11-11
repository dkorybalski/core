package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;

import java.util.Optional;

@Repository
public interface EvaluationCardTemplateRepositoryDAO extends JpaRepository<EvaluationCardTemplate, Long> {
    Optional<EvaluationCardTemplate> findByStudyYear(String studyYear);

    boolean existsByStudyYear(String studyYear);
}
