package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "EXTERNAL_LINK_DEFINITION")
public class ExternalLinkDefinition extends AbstractEntity {

    private String name;

    private LocalDate deadline;

    private String studyYear;

}
