package pl.edu.amu.wmi.model;

public enum UserRoleType {

    /**
     * base role means: SUPERVISOR or STUDENT role
     */
    BASE,
    /**
     * special role means role with additional permissions: COORDINATOR or PROJECT_ADMIN; if user does not have a special role, the base one is taken into account
     */
    SPECIAL
}
