package org.chenile.stm.impl;

import org.chenile.stm.ConfigProvider;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigProviderImpl implements ConfigProvider {
    public ConfigProviderImpl(InputStream inputStream) throws Exception{
        properties.load(inputStream);
    }
    public ConfigProviderImpl(Properties properties){
        this.properties = properties;
    }
    public ConfigProviderImpl(){

    }


    private Properties properties = new Properties();

    @Override
    public String valueOf(String configProperty) {
        return properties.getProperty(configProperty);
    }

    public Map<String,String> getProperties(String prefix){
        Map<String,String> map = new HashMap<>();
        for (Map.Entry<Object,Object> entry:properties.entrySet()){
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith(prefix)){
                map.put(key,value);
            }
        }
        return map;
    }
    public void setProperty(String name, String value){
        this.properties.setProperty(name,value);
    }

    public void deleteProperty(String name){
        this.properties.remove(name);
    }

    public void clear(){
        this.properties.clear();
    }

    public void setProperties(String props) throws Exception{
        StringReader stringReader = new StringReader(props);
        this.properties.load(stringReader);
    }

}

