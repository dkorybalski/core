package pl.edu.amu.wmi.service.grade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.util.Optional;

@Service
public class GradeServiceImpl implements GradeService {

    private final ProjectDAO projectDAO;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;


    @Autowired
    public GradeServiceImpl(ProjectDAO projectDAO,
                            ProjectCriteriaSectionMapper projectCriteriaSectionMapper) {
        this.projectDAO = projectDAO;
        this.projectCriteriaSectionMapper = projectCriteriaSectionMapper;
    }

    @Override
    @Transactional
    public void updateSingleGrade(Grade grade, CriterionCategory newSelectedCriterionCategory) {
        Integer criterionPoints = CriterionCategory.getPoints(newSelectedCriterionCategory);
        CriteriaGroup gradeCriteriaGroup = grade.getCriteriaGroup();
        Optional<Criterion> criterion = gradeCriteriaGroup.getCriteria().stream()
                .filter(c -> c.getCriterionCategory().equals(newSelectedCriterionCategory))
                .findFirst();
        Double groupWeight = grade.getCriteriaGroup().getGradeWeight();

        if (criterion.isPresent()) {
            grade.setPoints(criterionPoints);
            grade.setPointsWithWeight(criterionPoints * groupWeight);
            grade.setDisqualifying(criterion.get().isDisqualifying());
        } else {
            grade.setPointsWithWeight(null);
            grade.setPoints(null);
            grade.setDisqualifying(false);
        }
    }

}
