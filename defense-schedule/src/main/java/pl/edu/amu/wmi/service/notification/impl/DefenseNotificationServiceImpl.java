package pl.edu.amu.wmi.service.notification.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.mapper.notification.UserInfoMapper;
import pl.edu.amu.wmi.model.UserInfoDTO;
import pl.edu.amu.wmi.service.NotificationService;
import pl.edu.amu.wmi.service.notification.DefenseNotificationService;
import pl.edu.amu.wmi.util.EMailTemplate;

import java.util.List;

@Service
@Slf4j
public class DefenseNotificationServiceImpl implements DefenseNotificationService {

    private final StudentDAO studentDAO;
    private final NotificationService notificationService;
    private final UserInfoMapper userInfoMapper;

    public DefenseNotificationServiceImpl(StudentDAO studentDAO, NotificationService notificationService, UserInfoMapper userInfoMapper) {
        this.studentDAO = studentDAO;
        this.notificationService = notificationService;
        this.userInfoMapper = userInfoMapper;
    }

    @Override
    public void notifyStudents(String studyYear, DefensePhase defensePhase) {
        List<Student> students = studentDAO.findAllByStudyYear(studyYear);
        List<UserInfoDTO> userInfos = userInfoMapper.mapToUserInfos(students);
        switch (defensePhase) {
            case DEFENSE_PROJECT_REGISTRATION -> notificationService.sendEmails(userInfos, EMailTemplate.PROJECT_DEFENSE_REGISTRATION_OPEN);
            default -> log.info("Sending notification for defense: {} phase not supported", defensePhase);
        }
    }

    @Override
    public void notifyStudentsAboutProjectDefenseAssignment(List<Student> students) {
        List<UserInfoDTO> userInfos = userInfoMapper.mapToUserInfos(students);
        notificationService.sendEmails(userInfos, EMailTemplate.PROJECT_DEFENSE_ASSIGNMENT_CHANGE);
    }
}
