package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.UserInfoDTO;
import pl.edu.amu.wmi.util.EMailTemplate;

import java.util.List;

public interface NotificationService {

    void sendEmail(UserInfoDTO userInfo, EMailTemplate eMailTemplate);

    void sendEmails(List<UserInfoDTO> userInfos, EMailTemplate eMailTemplate);

}
