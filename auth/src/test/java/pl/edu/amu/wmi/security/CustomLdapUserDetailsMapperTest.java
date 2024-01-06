package pl.edu.amu.wmi.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.edu.amu.wmi.util.TestUtil.createUserData;

@ExtendWith(MockitoExtension.class)
class CustomLdapUserDetailsMapperTest {

    @Mock
    private UserDataDAO userDataDAO;

    @Mock
    private DirContextOperations dirContextOperations;

    @InjectMocks
    private CustomLdapUserDetailsMapper customLdapUserDetailsMapper;

    @Test
    void mapUserFromContext_successful() {
        //given
        String username = "s123456";
        UserData userData = createUserData(username, List.of(UserRole.COORDINATOR));
        Mockito.when(userDataDAO.findByIndexNumber(username)).thenReturn(Optional.of(userData));
        Mockito.when(dirContextOperations.getNameInNamespace()).thenReturn(username);
        //when
        UserDetails userDetails = customLdapUserDetailsMapper.mapUserFromContext(dirContextOperations, username, new HashSet<>());
        //then
        Mockito.verify(userDataDAO).findByIndexNumber(username);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    void mapUserFromContext_userNotFound_negative() {
        //given
        String username = "s123456";
        Mockito.when(userDataDAO.findByIndexNumber(username)).thenReturn(Optional.ofNullable(null));
        //when
        assertThrows(UsernameNotFoundException.class, () -> customLdapUserDetailsMapper.mapUserFromContext(dirContextOperations, username, new HashSet<>()));
        //then
        Mockito.verify(userDataDAO).findByIndexNumber(username);
    }
}
