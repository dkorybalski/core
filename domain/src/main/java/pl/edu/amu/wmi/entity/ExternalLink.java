package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "EXTERNAL_LINK")
public class ExternalLink extends AbstractEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "EXTERNAL_LINK_DEFINITION_ID")
    private ExternalLinkDefinition externalLinkDefinition;

    private String link;

}
