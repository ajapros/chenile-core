package org.chenile.stm.impl;


import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class TransitionStyler {
    Properties props = new Properties();

    public void loadFromXML(InputStream inputStream) throws Exception{
        props.loadFromXML(inputStream);
    }

    public void loadFromXML(String s) throws Exception{
         loadFromXML(new ByteArrayInputStream(s.getBytes()));
    }

    public String getStyle(StateDescriptor sd){
        return getStyle(sd.getMetadata());
    }

    public String getStyle(Transition t) {
        return getStyle(t.getMetadata());
    }

    public String getStyle(Map<String,String> md){
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
            String key = (String) prop.getKey();
            String[] arr = key.split("==");
            String value = (String) prop.getValue();
            if (arr[1].equals(md.get(arr[0]))) {
                return value;
            }
        }
        return "";
    }
}
