package pl.edu.amu.wmi.dao;

import jakarta.persistence.Tuple;

import java.util.List;

public interface ChairpersonDAO {

    List<Tuple> findCommitteeChairpersonsPerDayAndPerStudyYear(String studyYear);

}
