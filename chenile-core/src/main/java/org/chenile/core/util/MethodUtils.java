package org.chenile.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.core.util.convert.ChenileTypeUtils;

/**
 * Given an OperationDefinition and a service class, this class computes the java.lang.reflect.Method.
 */
public abstract class MethodUtils {
	/**
	 * Note that this uses the {@link ParamDefinition#getParamClass()} to compute the method.
	 * This is because the param class will return the definition of the request body in the actual class
	 * and not in  the context of the instance. for example consider the following code:
	 * <code>
	 *     public class A<T extends Foo>{
	 *         public void consumer(T t){}
	 *     }
	 *     public class Bar extends Foo{}
	 *
	 *    @Bean public B b() { return new A<Bar>();}
	 * </code>
	 * In the above case,if I have an instance of the bean "b" then we should look for a method "consumer"
	 * with param class of type Foo not of type Bar. paramClass() will have A.class and paramType() will have
	 * B.class
	 * @param clazz - the class whose method we need to compute
	 * @param od - the operation definition that defines the method's name and other details
	 * @return - the method that needs to be invoked. Null if the method is not found.
	 */
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
