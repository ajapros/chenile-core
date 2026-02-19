package org.chenile.multids.test.repository;

import org.chenile.multids.test.model.TenantItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantItemRepository extends JpaRepository<TenantItem, Long> {
}
