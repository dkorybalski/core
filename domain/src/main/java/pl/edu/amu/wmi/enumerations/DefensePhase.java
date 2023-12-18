package pl.edu.amu.wmi.enumerations;

import lombok.Getter;

@Getter
public enum DefensePhase {

    /**
     * phase when supervisors fill the accessibility surveys and coordinator creates a defense schedule
     */
    SCHEDULE_PLANNING("DEFENSE SCHEDULE PLANNING"),
    /**
     * phase when students choose the time slot of defense for project
     */
    DEFENSE_PROJECT_REGISTRATION("DEFENSE REGISTRATION OPEN"),
    /**
     * phase when student cannot modify the defense slots, but coordinator still can make modifications; in this phase
     * project defenses take places
     */
    DEFENSE_PROJECT("REGISTRATION CLOSED"),
    /**
     * phase when the defense process is closed - all project defense took place; in this phase edition is disabled
     */
    CLOSED("DEFENSE SCHEDULE CLOSED");

    final String phaseName;

    DefensePhase(String phaseName) {
        this.phaseName = phaseName;
    }
}
