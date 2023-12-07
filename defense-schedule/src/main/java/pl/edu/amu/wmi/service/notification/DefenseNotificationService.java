package pl.edu.amu.wmi.service.notification;

import pl.edu.amu.wmi.enumerations.DefensePhase;

public interface DefenseNotificationService {

    void notifyStudents(String studyYear, DefensePhase defensePhase);
}
