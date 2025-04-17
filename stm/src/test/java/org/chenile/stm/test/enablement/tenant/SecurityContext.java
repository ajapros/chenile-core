package org.chenile.stm.test.enablement.tenant;

public class SecurityContext {

    private String tenant;

    public SecurityContext() {
        this.tenant = "system";
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTenant() {
        return tenant;
    }
}
