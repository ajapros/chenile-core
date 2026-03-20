package org.chenile.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.core.util.convert.ChenileTypeUtils;

/**
 * Given an OperationDefinition and a service class, this class computes the java.lang.reflect.Method
 */
public abstract class MethodUtils {
	public static Method computeMethod( Class<?> clazz, OperationDefinition od) {
		List<Class<?>> paramTypes = new ArrayList<Class<?>>();
		for (ParamDefinition pd: od.getParams()) {
			Class<?> paramClass = pd.getParamClass();
			if (paramClass == null) {
				paramClass = ChenileTypeUtils.toRawClass(pd.getParamType());
			}
			paramTypes.add(paramClass);
		}
		Class<?>[] parameterTypes = new Class<?>[paramTypes.size()];
    	try {
    		return clazz.getMethod(od.getMethodName(),
    			paramTypes.toArray(parameterTypes));
    	}catch(NoSuchMethodException e) {
    		return null;
    	}
		
	}
}
