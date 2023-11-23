package pl.edu.amu.wmi.service.grade.impl;

import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.service.grade.EvaluationCardPermissionService;

@Service
public class EvaluationCardPermissionServiceImpl implements EvaluationCardPermissionService {
    
    
    @Override
    public boolean isUserAllowedToSeeEvaluationDetails(String studyYear, String username) {
        // TODO: 11/23/2023 implement this method
        return false;
    }
}
