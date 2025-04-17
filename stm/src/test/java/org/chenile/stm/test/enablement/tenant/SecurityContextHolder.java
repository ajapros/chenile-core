    package org.chenile.stm.test.enablement.tenant;

public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> contextHolder = ThreadLocal.withInitial(SecurityContext::new);

    public static SecurityContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
