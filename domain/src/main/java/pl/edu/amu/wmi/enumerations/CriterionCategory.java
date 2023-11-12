package pl.edu.amu.wmi.enumerations;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CriterionCategory {
    CRITERION_NOT_MET(0, 0),
    UNSUCCESSFUL_ATTEMPT_TO_MEET_THE_CRITERION(1, 1),
    CRITERION_MET_WITH_RESERVATIONS(2, 3),
    CRITERION_MET(3, 4);

    final Integer points;

    @Getter
    final Integer value;

    CriterionCategory(Integer value, Integer points) {
        this.value = value;
        this.points = points;
    }

    private static final Map<Integer, CriterionCategory> criterionCategories;
    static {
        criterionCategories = new HashMap<>();
        for (CriterionCategory category : CriterionCategory.values()) {
            criterionCategories.put(category.points, category);
        }
    }

    public static CriterionCategory findByPointsReceived(Integer points) {
        return criterionCategories.get(points);
    }

}
