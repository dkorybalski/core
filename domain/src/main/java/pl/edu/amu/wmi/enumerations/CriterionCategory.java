package pl.edu.amu.wmi.enumerations;

import java.util.HashMap;
import java.util.Map;

public enum CriterionCategory {
    CRITERION_NOT_MET(0),
    UNSUCCESSFUL_ATTEMPT_TO_MEET_THE_CRITERION(1),
    CRITERION_MET_WITH_RESERVATIONS(3),
    CRITERION_MET(4);

    final Integer points;

    CriterionCategory(Integer points) {
        this.points = points;
    }

    private static final Map<Integer, CriterionCategory> criterionCategories;

    static {
        criterionCategories = new HashMap<>();
        for (CriterionCategory category : CriterionCategory.values()) {
            criterionCategories.put(category.points, category);
        }
    }

    public static CriterionCategory getByPointsReceived(Integer points) {
        if (points != null)
            return criterionCategories.get(points);
        else
            return null;
    }

    public static Integer getPoints(CriterionCategory criterion) {
        if (criterion != null)
            return criterion.points;
        else
            return null;
    }

}
