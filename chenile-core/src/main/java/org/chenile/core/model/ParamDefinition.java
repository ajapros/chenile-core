package org.chenile.core.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.chenile.core.util.convert.ChenileTypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Defines an individual parameter that is accepted by an operation of a sevice.
 */
public class ParamDefinition implements Serializable {

     private static final long serialVersionUID = 1L;

    String name;
    HttpBindingType type;
    String description;
    Class<?> paramClass;
    Type paramType;
    
    /**
     * Raw invocation type used internally for method matching and runtime conversion.
     * External consumers should use {@link #getParamType()} instead.
     */
    public Class<?> getParamClass() {
		if (paramClass == null && paramType != null) {
            paramClass = ChenileTypeUtils.toRawClass(paramType);
        }
		return paramClass;
	}

    /**
     * Internal setter for the raw invocation type. External metadata producers should prefer
     * {@link #setParamType(Type)} so generic type information is preserved.
     */
	public void setParamClass(Class<?> paramClass) {
		this.paramClass = paramClass;
        if (this.paramType == null) {
            this.paramType = paramClass;
        }
	}

    /**
     * The public metadata type for the parameter. External consumers should use this accessor
     * instead of {@link #getParamClass()} because it preserves generic type information.
     */
    @JsonIgnore
    public Type getParamType() {
        return (paramType != null) ? paramType : paramClass;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
        this.paramClass = ChenileTypeUtils.toRawClass(paramType);
    }

    @JsonGetter("paramType")
    public String getParamTypeAsString() {
        return ChenileTypeUtils.typeToString(getParamType());
    }

    @JsonSetter("paramType")
    public void setParamTypeAsString(String paramTypeName) throws ClassNotFoundException {
        if (paramTypeName == null || paramTypeName.isBlank()) {
            this.paramType = null;
            this.paramClass = null;
            return;
        }
        setParamType(ChenileTypeUtils.parseType(paramTypeName));
    }

	boolean cacheKey = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HttpBindingType getType() {
        return type;
    }

    public void setType(HttpBindingType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(boolean cacheKey) {
        this.cacheKey = cacheKey;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\t<param>\n");
        sb.append("\t\t\t<name>" + name + "</name>\n");
        sb.append("\t\t\t<type>" + type + "</type>\n");
        sb.append("\t\t\t<description>" + description + "</description>\n");
        sb.append("\t\t</param>\n");
        return sb.toString();
    }
}
