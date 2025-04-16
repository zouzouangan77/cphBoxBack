package com.psu.rouen.cphbox.repository.search;

import com.psu.rouen.cphbox.domain.Catalog;
import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Data Elasticsearch repository for the Catalog entity.
 */

interface CatalogSearchRepositoryInternal {
    Stream<Catalog> search(String query);
}
