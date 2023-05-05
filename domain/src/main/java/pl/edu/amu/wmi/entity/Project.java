package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROJECT")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

}
