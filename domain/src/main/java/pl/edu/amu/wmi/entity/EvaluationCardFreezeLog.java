package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.AutomaticActionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EVALUATION_CARD_FREEZE_LOG")
public class EvaluationCardFreezeLog extends AbstractEntity {

    private Long projectId;

    private Long projectDefenseId;

    @Enumerated(EnumType.STRING)
    private AutomaticActionStatus automaticActionStatus;

    private String message;

}
