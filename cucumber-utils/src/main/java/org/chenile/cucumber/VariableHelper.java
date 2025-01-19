package org.chenile.cucumber;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Class manages the variables that are set and used in Cucumber test cases.
 * Contains Variable substitution helper methods
 */
public class VariableHelper {
    /**
     * Uses a variable to store the results of a scenario so that it can be used in the next scenario.<br/>
     * For example, if we create an entity and want to retrieve the same entity by ID, then we can store
     * the ID as a variable in the varMap. We can use the ID to retrieve the object back in the next scenario<br/>
     * varMap spans scenarios and hence needs to be stored outside the context.<br/>
     */
    private static final Map<String,String> varMap = new HashMap<String, String>();
    public static void put(String name, String value){
        varMap.put(name,value);
    }

    public static String substituteVariables(String s) {
        if (varMap.isEmpty()) return s;
        StringSubstitutor sub = new StringSubstitutor(varMap);
        return sub.replace(s);
    }
}
