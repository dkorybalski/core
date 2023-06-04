package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.Role;
import pl.edu.amu.wmi.enumerations.UserRole;

@Repository
public interface RoleDAO extends JpaRepository<Role, Long> {

    Role findByName(UserRole name);

}
