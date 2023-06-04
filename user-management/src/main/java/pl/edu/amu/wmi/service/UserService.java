package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.user.UserDTO;

public interface UserService {

    UserDTO getUser(String indexNumber);

}
