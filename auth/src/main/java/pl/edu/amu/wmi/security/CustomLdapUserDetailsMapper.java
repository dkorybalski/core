package pl.edu.amu.wmi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.Role;
import pl.edu.amu.wmi.entity.UserData;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper {

    @Autowired
    UserDataDAO userDataDAO;
    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String   username,
                                          Collection<? extends GrantedAuthority> authorities) {
        UserData user = userDataDAO.findByIndexNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format("User with index number not found: {0}", username)));

        UserDetails userDetails = super.mapUserFromContext(ctx, username, getAuthorities(user));
        return new UserDetailsImpl(user.getId(), userDetails.getUsername(), user.getEmail(), userDetails.getAuthorities());
    }

    private Set<GrantedAuthority> getAuthorities(UserData user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        //populate authorities/user roles from UserData (db entity)
        for (Role userRole : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(userRole.getName().name()));
        }

        return authorities;
    }

}
