package pl.edu.amu.wmi.model.diploma;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaAddOrUpdateDTO {
    private String titleEn;
    private String titlePl;
    private String description;
    private Integer projectId;
}
