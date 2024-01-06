package pl.edu.amu.wmi.util;

import org.springframework.http.ResponseCookie;
import pl.edu.amu.wmi.entity.Role;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public static ResponseCookie createCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "value").path("/").maxAge(24 * 60 * 60).httpOnly(true).build();
    }

    public static UserData createUserData(String username, List<UserRole> userRoles) {
        UserData userData = new UserData();
        userData.setIndexNumber(username);
        Set<Role> roles = new HashSet<>();
        userRoles.forEach(userRole -> {
            Role role = new Role();
            role.setName(userRole);
            roles.add(role);
        });
        userData.setRoles(roles);
        return userData;
    }
}
