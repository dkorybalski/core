package pl.edu.amu.wmi.service.notification;

import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.enumerations.DefensePhase;

import java.util.List;

public interface DefenseNotificationService {

    /**
     * Sends an email notification to all students from a study year.
     * The message content depends on the defense phase
     *
     * @param studyYear    - study year used to fetch students data
     * @param defensePhase - the phase of project defense process
     */
    void notifyStudents(String studyYear, DefensePhase defensePhase);

    /**
     * Sends an email notification to students associated with a project for which the project defense slot has been changed
     *
     * @param students - students who should be notified of the changes
     */
    void notifyStudentsAboutProjectDefenseAssignment(List<Student> students);
}
