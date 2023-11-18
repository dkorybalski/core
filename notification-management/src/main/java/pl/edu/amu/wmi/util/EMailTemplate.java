package pl.edu.amu.wmi.util;

import lombok.Getter;

@Getter
public enum EMailTemplate {

    MISSING_PROJECT_ASSESSMENTS("missing_project_assessment_template.ftlh", "PRI - Missing project assessments");

    private final String path;
    private final String subject;

    EMailTemplate(String path, String subject) {
        this.path = path;
        this.subject = subject;
    }
}
