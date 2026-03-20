package org.chenile.core.util.convert;


import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class ChenileGenericTypeParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JavaType parse(String typeStr) throws ClassNotFoundException {
        return parseRecursive(typeStr);
    }

    private static JavaType parseRecursive(String typeStr) throws ClassNotFoundException {
        typeStr = typeStr.trim();

        int genericStart = typeStr.indexOf('<');
        if (genericStart == -1) {
            // no generics, just a raw class
            return mapper.getTypeFactory().constructType(resolveClass(typeStr));
        }

        String rawClassName = typeStr.substring(0, genericStart).trim();
        Class<?> rawClass = resolveClass(rawClassName);

        // extract the generic content inside <...>
        String inner = typeStr.substring(genericStart + 1, typeStr.lastIndexOf('>')).trim();
        List<String> parts = splitTopLevel(inner);

        List<JavaType> paramTypes = new ArrayList<>();
        for (String part : parts) {
            paramTypes.add(parseRecursive(part));
        }

        return mapper.getTypeFactory()
                .constructParametricType(rawClass, paramTypes.toArray(new JavaType[0]));
    }

    // Split by commas, but only at top-level (not inside nested < >)
    private static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        result.add(current.toString().trim());
        return result;
    }

    private static Class<?> resolveClass(String typeName) throws ClassNotFoundException {
        return switch (typeName) {
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "char" -> char.class;
            case "void" -> void.class;
            default -> Class.forName(typeName);
        };
    }

}
