package pl.edu.amu.wmi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.UserData;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserDataDAO userDataDAO;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String indexNumber) throws UsernameNotFoundException {
        UserData user = userDataDAO.findByIndexNumber(indexNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User with index number not found: " + indexNumber));
        return UserDetailsImpl.build(user);
    }
}
