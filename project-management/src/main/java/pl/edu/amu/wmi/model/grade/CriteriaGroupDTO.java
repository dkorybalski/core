package pl.edu.amu.wmi.model.grade;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CriterionCategory;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
public class CriteriaGroupDTO {

    @NotNull
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CriterionCategory selectedCriterion;

    private String gradeWeight;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy")
    private LocalDate modificationDate;

    private Map<String, CriterionDTO> criteria;

}
