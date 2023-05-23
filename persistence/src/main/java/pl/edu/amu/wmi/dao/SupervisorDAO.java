package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.Supervisor;

public interface SupervisorDAO extends JpaRepository<Supervisor, Long> {
}
