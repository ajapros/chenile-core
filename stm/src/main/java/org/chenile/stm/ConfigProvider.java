package org.chenile.stm;

import java.util.Map;

public interface ConfigProvider {
    public String valueOf(String configProperty);
    public Map<String,String> getProperties(String prefix);
}
