package com.psu.rouen.cphbox.config.dbmigrations;

import com.psu.rouen.cphbox.domain.Authority;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "authority-initialization", order = "003")
public class NewAuthority20230524 {

    private final MongoTemplate template;

    public NewAuthority20230524(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Authority> authorities = createUserAuthorities();
        template.insertAll(authorities);
    }

    @RollbackExecution
    public void rollback() {}

    private List<Authority> createUserAuthorities() {
        Authority boxAuthority = createAuthority(AuthoritiesConstants.BOX_CRUD);
        Authority catalogAuthority = createAuthority(AuthoritiesConstants.CATALOG_CRUD);
        Authority warehouseAuthority = createAuthority(AuthoritiesConstants.WAREHOUSE_CRUD);
        Authority booksViewAuthority = createAuthority(AuthoritiesConstants.BOOKS_VIEW);
        return Arrays.asList(boxAuthority, catalogAuthority, warehouseAuthority);
    }

    private Authority createAuthority(String authority) {
        Authority adminAuthority = new Authority();
        adminAuthority.setName(authority);
        return adminAuthority;
    }
}
