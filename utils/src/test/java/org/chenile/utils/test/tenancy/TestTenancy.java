package org.chenile.utils.test.tenancy;

import org.chenile.utils.tenancy.TenantSpecificResourceLoader;
import org.junit.Test;

public class TestTenancy {
    @Test public void testIt(){
        String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.xml";
        String genericPath = "org/chenile/test/test-resource.xml";
        TenantSpecificResourceLoader loader = new TenantSpecificResourceLoader(tenantSpecificPath,genericPath);
        System.out.println(loader.obtainFileName("","abc"));
    }
}
