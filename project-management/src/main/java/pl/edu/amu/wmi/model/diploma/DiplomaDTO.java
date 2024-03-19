package pl.edu.amu.wmi.model.diploma;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaDTO {
    private String titleEn;
    private String titlePl;
    private String description;
    private Integer projectId;
    private List<DiplomaChapterDTO> chapters;
}
