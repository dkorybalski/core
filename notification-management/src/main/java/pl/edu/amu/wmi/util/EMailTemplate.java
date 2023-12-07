package pl.edu.amu.wmi.util;

import lombok.Getter;

@Getter
public enum EMailTemplate {

    PROJECT_DEFENSE_REGISTRATION_OPEN("project_defense_registration_open.ftlh", "PRI - Registration for project defenses has been open");

    private final String path;
    private final String subject;

    EMailTemplate(String path, String subject) {
        this.path = path;
        this.subject = subject;
    }
}
