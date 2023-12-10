package pl.edu.amu.wmi.model.supervisordefense;

import java.util.Map;

public record SupervisorStatisticsDTO(
        String supervisor,

        Integer numberOfGroups,

        Integer totalNumberOfCommittees,

        /**
         * result of totalNumberOfCommittees / numberOfGroups
         */
        Double load,

        Map<String, Integer> committeesPerDay
) {
}
