package pl.edu.amu.wmi.model.diploma;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaChapterAddOrUpdateDTO {
    private String title;
    private String description;
    private Integer projectId;
    private String studentIndex;
}