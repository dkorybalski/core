package pl.edu.amu.wmi.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.dao.ChairpersonDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ChairpersonDAOImpl implements ChairpersonDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Tuple> findCommitteeChairpersonsPerDayAndPerStudyYear(String studyYear) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();

        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<SupervisorDefenseAssignment> supervisorDefenseAssignmentMetaModel = metamodel.entity(SupervisorDefenseAssignment.class);

        Root<SupervisorDefenseAssignment> root = criteriaQuery.from(SupervisorDefenseAssignment.class);
        Join<SupervisorDefenseAssignment, DefenseTimeSlot> defenseTimeSlotJoin =
                root.join(supervisorDefenseAssignmentMetaModel.getSingularAttribute("defenseTimeSlot", DefenseTimeSlot.class));

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.isTrue(root.get("isChairperson")));
        predicates.add(criteriaBuilder.equal(defenseTimeSlotJoin.get("studyYear"), studyYear));

        criteriaQuery.multiselect(
                        root.get("supervisor").alias("supervisor"),
                        root.get("committeeIdentifier").alias("committeeIdentifier"),
                        root.get("classroom").alias("classroom"),
                        defenseTimeSlotJoin.get("date").alias("date"))
                .where(predicates.toArray(new Predicate[0]))
                .groupBy(root.get("supervisor"), root.get("committeeIdentifier"), root.get("classroom"), defenseTimeSlotJoin.get("date"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
