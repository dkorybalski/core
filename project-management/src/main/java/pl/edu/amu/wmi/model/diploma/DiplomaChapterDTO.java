package pl.edu.amu.wmi.model.diploma;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaChapterDTO {
    private String title;
    private String description;
    private String studentIndex;
}
