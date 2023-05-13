package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.Instructor;

public interface InstructorDAO extends JpaRepository<Instructor, Long> {
}
