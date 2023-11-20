package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.SessionData;

import java.util.Optional;

@Repository
public interface SessionDataDAO extends JpaRepository<SessionData, Long> {

    Optional<SessionData> findByUserData_IndexNumber(String indexNumber);

}
