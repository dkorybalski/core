package pl.edu.amu.wmi.enumerations;

public enum DefensePhase {

    /**
     * phase when supervisors fill the accessibility surveys and coordinator creates a defense schedule
     */
    SCHEDULE_PLANNING,
    /**
     * phase when students choose the time slot of defense for project
     */
    DEFENSE_PROJECT_REGISTRATION,
    /**
     * phase when student cannot modify the defense slots, but coordinator still can make modifications; in this phase
     * project defenses take places
     */
    DEFENSE_PROJECT,
    /**
     * phase when the defense process is closed - all project defense took place; in this phase edition is disabled
     */
    CLOSED

}
