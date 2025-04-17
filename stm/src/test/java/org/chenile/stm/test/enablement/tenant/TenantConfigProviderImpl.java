package org.chenile.stm.test.enablement.tenant;

import org.chenile.stm.ConfigProvider;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TenantConfigProviderImpl implements ConfigProvider {

    private static Map<String,Properties> propertiesMap = new HashMap<>();



    public TenantConfigProviderImpl() {
        try {
            Properties t2 = new Properties();
            t2.load(new StringReader("""
                    MFG_FLOW.S1.enabled=false
                    # Add transition doS2 to CREATED state that will lead to state S2
                    MFG_FLOW.CREATED.transition.add.doS2=S2
                    """));
            propertiesMap.put("t2",t2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        propertiesMap.put("t1",new Properties());
        propertiesMap.put("system",new Properties());

    }


    @Override
    public String valueOf(String configProperty) {
        return propertiesMap.get(SecurityContextHolder.getContext().getTenant())
                .getProperty(configProperty);
    }

    public Map<String,String> getProperties(String prefix){
        Map<String,String> map = new HashMap<>();
        for (Map.Entry<Object,Object> entry:propertiesMap.get(SecurityContextHolder.getContext().getTenant()).entrySet()){
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith(prefix)){
                map.put(key,value);
            }
        }
        return map;
    }
    public void setProperty(String name, String value){
        this.propertiesMap.get(SecurityContextHolder.getContext().getTenant()).setProperty(name,value);
    }

    public void deleteProperty(String name){
        this.propertiesMap.get(SecurityContextHolder.getContext().getTenant()).remove(name);
    }

    public void clear(){
        System.out.println("###### Clear #####");
        this.propertiesMap.get(SecurityContextHolder.getContext().getTenant()).clear();
    }

    public void setProperties(String props) throws Exception{
        StringReader stringReader = new StringReader(props);
        this.propertiesMap.get(SecurityContextHolder.getContext().getTenant()).load(stringReader);
    }

}
