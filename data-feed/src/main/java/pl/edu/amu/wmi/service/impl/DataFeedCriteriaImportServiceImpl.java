package pl.edu.amu.wmi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;
import pl.edu.amu.wmi.service.criteria.CriteriaSaveService;
import pl.edu.amu.wmi.service.criteria.CriteriaUpdateService;
import pl.edu.amu.wmi.service.criteria.EvaluationCardTemplateService;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DataFeedCriteriaImportServiceImpl implements DataFeedImportService {

    private final CriteriaSaveService criteriaSaveService;
    private final CriteriaUpdateService criteriaUpdateService;
    private final EvaluationCardTemplateService evaluationCardTemplateService;

    public DataFeedCriteriaImportServiceImpl(CriteriaSaveService criteriaSaveService, CriteriaUpdateService criteriaUpdateService, EvaluationCardTemplateService evaluationCardTemplateService) {
        this.criteriaSaveService = criteriaSaveService;
        this.criteriaUpdateService = criteriaUpdateService;
        this.evaluationCardTemplateService = evaluationCardTemplateService;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.CRITERIA;
    }

    @Override
    @Transactional
    public void saveRecords(MultipartFile data, String studyYear) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            EvaluationCriteriaDTO evaluationCriteriaDTO = objectMapper.readValue(data.getInputStream(), EvaluationCriteriaDTO.class);
            if (!Objects.equals(studyYear, evaluationCriteriaDTO.studyYear())) {
                log.warn("Study year from file is different that active one. Study year from file is ignored");
            }
            List<CriteriaSectionDTO> criteriaSections = evaluationCriteriaDTO.criteriaSections();

            boolean isEvaluationCardTemplatePresentForStudyYear = evaluationCardTemplateService.existsByStudyYear(studyYear);
            if (!isEvaluationCardTemplatePresentForStudyYear) {
                EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateService.saveEvaluationCardTemplate(studyYear, evaluationCriteriaDTO);
                criteriaSections.forEach(criteriaSection -> criteriaSaveService.saveCriteriaSection(criteriaSection, evaluationCardTemplate));
            } else {
                EvaluationCardTemplate updatedEvaluationCardTemplate = evaluationCardTemplateService.updateEvaluationCardTemplate(studyYear, evaluationCriteriaDTO);
                criteriaSections.forEach(criteriaSection -> criteriaUpdateService.updateCriteriaSection(criteriaSection, updatedEvaluationCardTemplate));
            }
        } catch (Exception exception) {
            log.error("Exception during parsing the criteria");
            throw exception;
        }
    }

}
