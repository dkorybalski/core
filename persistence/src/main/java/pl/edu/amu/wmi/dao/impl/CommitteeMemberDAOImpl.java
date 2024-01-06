package pl.edu.amu.wmi.dao.impl;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.dao.CommitteeMemberDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.CommitteeAssignmentCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class CommitteeMemberDAOImpl implements CommitteeMemberDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String entityGraphFull = "graph.SupervisorDefenseAssignment.FULL";

    @Override
    public List<Tuple> findCommitteeChairpersonsPerDayAndPerStudyYear(String studyYear) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();

        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<SupervisorDefenseAssignment> supervisorDefenseAssignmentMetaModel = metamodel.entity(SupervisorDefenseAssignment.class);
        EntityType<DefenseTimeSlot> defenseTimeSlotMetaModel = metamodel.entity(DefenseTimeSlot.class);

        Root<SupervisorDefenseAssignment> root = criteriaQuery.from(SupervisorDefenseAssignment.class);
        Join<SupervisorDefenseAssignment, DefenseTimeSlot> defenseTimeSlotJoin =
                root.join(supervisorDefenseAssignmentMetaModel.getSingularAttribute("defenseTimeSlot", DefenseTimeSlot.class));
        Join<DefenseTimeSlot, DefenseScheduleConfig> defenseScheduleConfigJoin =
                defenseTimeSlotJoin.join(defenseTimeSlotMetaModel.getSingularAttribute("defenseScheduleConfig", DefenseScheduleConfig.class));

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.isTrue(root.get("isChairperson")));
        predicates.add(criteriaBuilder.equal(defenseTimeSlotJoin.get("studyYear"), studyYear));
        predicates.add(criteriaBuilder.isTrue(defenseScheduleConfigJoin.get("isActive")));

        criteriaQuery.multiselect(
                        root.get("supervisor").alias("supervisor"),
                        root.get("committeeIdentifier").alias("committeeIdentifier"),
                        root.get("classroom").alias("classroom"),
                        defenseTimeSlotJoin.get("date").alias("date"))
                .where(predicates.toArray(new Predicate[0]))
                .groupBy(root.get("supervisor"), root.get("committeeIdentifier"), root.get("classroom"), defenseTimeSlotJoin.get("date"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public List<SupervisorDefenseAssignment> findAllAssignmentsByCriteria(CommitteeAssignmentCriteria criteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SupervisorDefenseAssignment> criteriaQuery = criteriaBuilder.createQuery(SupervisorDefenseAssignment.class);

        EntityGraph entityGraph = entityManager.getEntityGraph(entityGraphFull);

        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<SupervisorDefenseAssignment> supervisorDefenseAssignmentMetaModel = metamodel.entity(SupervisorDefenseAssignment.class);

        Root<SupervisorDefenseAssignment> root = criteriaQuery.from(SupervisorDefenseAssignment.class);
        Join<SupervisorDefenseAssignment, DefenseTimeSlot> defenseTimeSlotJoin =
                root.join(supervisorDefenseAssignmentMetaModel.getSingularAttribute("defenseTimeSlot", DefenseTimeSlot.class));
        Join<SupervisorDefenseAssignment, Supervisor> supervisorJoin =
                root.join(supervisorDefenseAssignmentMetaModel.getSingularAttribute("supervisor", Supervisor.class));

        List<Predicate> predicates = createPredicates(criteriaBuilder, root, defenseTimeSlotJoin, supervisorJoin, criteria);

        criteriaQuery.select(root)
                .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).setHint("javax.persistence.fetchgraph", entityGraph).getResultList();
    }

    private List<Predicate> createPredicates(CriteriaBuilder criteriaBuilder, Root<SupervisorDefenseAssignment> root, Join<SupervisorDefenseAssignment, DefenseTimeSlot> defenseTimeSlotJoin,
                                             Join<SupervisorDefenseAssignment, Supervisor> supervisorJoin, CommitteeAssignmentCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(criteria.getStudyYear())) {
            predicates.add(criteriaBuilder.equal(defenseTimeSlotJoin.get("studyYear"), criteria.getStudyYear()));
        }
        if (Objects.nonNull(criteria.getDate())) {
            predicates.add(criteriaBuilder.equal(defenseTimeSlotJoin.get("date"), criteria.getDate()));
        }
        if (Objects.nonNull(criteria.getCommitteeIdentifier())) {
            predicates.add(criteriaBuilder.equal(root.get("committeeIdentifier"), criteria.getCommitteeIdentifier()));
        }
        if (Objects.nonNull(criteria.getSupervisorId())) {
            predicates.add(criteriaBuilder.equal(supervisorJoin.get("id"), criteria.getSupervisorId()));
        }
        if (Objects.nonNull(criteria.getDefenseTimeslotId())) {
            predicates.add(criteriaBuilder.equal(defenseTimeSlotJoin.get("id"), criteria.getDefenseTimeslotId()));
        }
        if (Objects.nonNull(criteria.getExcludedSupervisorIds()) && !criteria.getExcludedSupervisorIds().isEmpty()) {
            predicates.add(criteriaBuilder.not(supervisorJoin.get("id").in(criteria.getExcludedSupervisorIds())));
        }
        if (Objects.nonNull(criteria.getIsChairperson()) && Objects.equals(Boolean.TRUE, criteria.getIsChairperson())) {
            predicates.add(criteriaBuilder.isTrue(root.get("isChairperson")));
        }
        return predicates;
    }
}
