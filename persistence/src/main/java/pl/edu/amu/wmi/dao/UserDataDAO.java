package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.UserData;

@Repository
public interface UserDataDAO extends JpaRepository<UserData, Long> {

    UserData findByIndexNumber(String index);

}
