package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.UserData;

import java.util.Optional;

@Repository
public interface UserDataDAO extends JpaRepository<UserData, Long> {

    Optional<UserData> findByIndexNumber(String index);

    Boolean existsByIndexNumber(String indexNumber);

    // TODO: 6/3/2023 is this method necessary??
    Boolean existsByEmail(String email);
}
