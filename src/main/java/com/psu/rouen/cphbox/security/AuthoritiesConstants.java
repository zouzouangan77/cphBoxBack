package com.psu.rouen.cphbox.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";
    public static final String BOX_CRUD = "ROLE_BOX_CRUD";
    public static final String ORDER_CRUD = "ROLE_ORDER_CRUD";
    public static final String ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String EVENT_CRUD = "ROLE_EVENT_CRUD";

    public static final String WAREHOUSE_CRUD = "ROLE_WAREHOUSE_CRUD";
    public static final String CATALOG_CRUD = "ROLE_CATALOG_CRUD";

    public static final String BOOKS_VIEW = "ROLE_BOOKS_VIEW";

    private AuthoritiesConstants() {}
}
