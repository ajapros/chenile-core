package org.chenile.multids.test.service;

import org.chenile.multids.test.model.TenantItem;
import org.chenile.multids.test.repository.TenantItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("tenantItemService")
public class TenantItemService {

    @Autowired
    private TenantItemRepository repository;

    public Map<String, Object> items() {
        List<TenantItem> items = repository.findAll();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", items);
        return payload;
    }
}
