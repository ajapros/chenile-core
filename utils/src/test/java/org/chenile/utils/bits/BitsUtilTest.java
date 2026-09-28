package org.chenile.utils.bits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitsUtilTest {

    @Test
    void setsAndReadsConfiguredBits() {
        byte[] bytes = new byte[100];

        BitsUtil.setScope(bytes, 8);
        BitsUtil.setScope(bytes, 85);
        BitsUtil.setScope(bytes, 102);
        BitsUtil.setScope(bytes, 299);

        assertTrue(BitsUtil.isScopeEnabled(bytes, 8));
        assertFalse(BitsUtil.isScopeEnabled(bytes, 9));
        assertTrue(BitsUtil.isScopeEnabled(bytes, 85));
        assertTrue(BitsUtil.isScopeEnabled(bytes, 102));
        assertTrue(BitsUtil.isScopeEnabled(bytes, 299));
    }
}
