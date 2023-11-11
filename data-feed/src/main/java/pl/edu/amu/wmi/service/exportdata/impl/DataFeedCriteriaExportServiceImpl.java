package pl.edu.amu.wmi.service.exportdata.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.criteria.EvaluationCriteriaService;
import pl.edu.amu.wmi.service.exportdata.DataFeedExportService;

import java.io.Writer;

@Component
@Slf4j
public class DataFeedCriteriaExportServiceImpl implements DataFeedExportService {

    private final EvaluationCriteriaService evaluationCriteriaService;

    public DataFeedCriteriaExportServiceImpl(EvaluationCriteriaService evaluationCriteriaService) {
        this.evaluationCriteriaService = evaluationCriteriaService;
    }

    @Override
    public void exportData(Writer writer, String studyYear) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        EvaluationCriteriaDTO evaluationCriteriaDTO = evaluationCriteriaService.constructEvaluationCriteriaDTO(studyYear);
        String content = objectMapper.writeValueAsString(evaluationCriteriaDTO);
        writer.write(content);
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.CRITERIA;
    }
}
