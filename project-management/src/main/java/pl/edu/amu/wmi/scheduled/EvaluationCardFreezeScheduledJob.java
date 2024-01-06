package pl.edu.amu.wmi.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.dao.EvaluationCardFreezeLogDAO;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.entity.EvaluationCardFreezeLog;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.enumerations.AutomaticActionStatus;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "pri.scheduling.enabled", havingValue = "true")
@Slf4j
public class EvaluationCardFreezeScheduledJob {

    private final ProjectDefenseDAO projectDefenseDAO;
    private final EvaluationCardService evaluationCardService;
    private final EvaluationCardFreezeLogDAO evaluationCardFreezeLogDAO;

    public EvaluationCardFreezeScheduledJob(ProjectDefenseDAO projectDefenseDAO, EvaluationCardService evaluationCardService, EvaluationCardFreezeLogDAO evaluationCardFreezeLogDAO) {
        this.projectDefenseDAO = projectDefenseDAO;
        this.evaluationCardService = evaluationCardService;
        this.evaluationCardFreezeLogDAO = evaluationCardFreezeLogDAO;
    }

    @Scheduled(cron = "0 0 6 * * *") // run every day at 6 a.m.
    public void freezeEvaluationCards() {
        LocalDate date = LocalDate.now();
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByDefenseDateAndProjectNotNull(date);
        List<Long> projectDefenseIds = projectDefenses.stream()
                .map(ProjectDefense::getId)
                .toList();
        log.info("Scheduled job freezeEvaluationCards runs for project defenses with ids: [{}]", projectDefenseIds);
        projectDefenses.forEach(projectDefense -> {
            try {
                evaluationCardService.freezeEvaluationCard(projectDefense.getProject().getId());
                EvaluationCardFreezeLog evaluationCardFreezeLog = new EvaluationCardFreezeLog(
                        projectDefense.getProject().getId(),
                        projectDefense.getId(),
                        AutomaticActionStatus.SUCCESSFUL,
                        ""
                );
                evaluationCardFreezeLogDAO.save(evaluationCardFreezeLog);
                log.info("Evaluation card connected with project {} was successfully frozen", projectDefense.getProject().getId());
            } catch (Exception e) {
                log.error("Freezing evaluation card connected with project: {} and project defense: {} unsuccessful",
                        projectDefense.getProject().getId(), projectDefense.getId(), e);
                EvaluationCardFreezeLog evaluationCardFreezeLog = new EvaluationCardFreezeLog(
                        projectDefense.getProject().getId(),
                        projectDefense.getId(),
                        AutomaticActionStatus.UNSUCCESSFUL,
                        e.getMessage()
                );
                evaluationCardFreezeLogDAO.save(evaluationCardFreezeLog);
            }
        });
    }
}

