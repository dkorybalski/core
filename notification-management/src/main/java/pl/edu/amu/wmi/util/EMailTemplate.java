package pl.edu.amu.wmi.util;

import lombok.Getter;

@Getter
public enum EMailTemplate {

    PROJECT_DEFENSE_REGISTRATION_OPEN("project_defense_registration_open.ftlh", "PRI - Registration for project defenses has been opened"),
    PROJECT_DEFENSE_ASSIGNMENT_CHANGE("project_defense_assignment_change.ftlh", "PRI - The project defense date has been changed");

    private final String path;
    private final String subject;

    EMailTemplate(String path, String subject) {
        this.path = path;
        this.subject = subject;
    }
}
