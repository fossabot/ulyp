package com.ulyp.agent;


import com.ulyp.agent.settings.MethodRepresentation;

public class MethodMatcher {

    private static final String WILDCARD = "*";

    private final String classSimpleName;
    private final String methodSimpleName;
    private final boolean isWildcard;

    public MethodMatcher(Class<?> clazz, String methodSimpleName) {
        this.classSimpleName = clazz.getSimpleName();
        this.methodSimpleName = methodSimpleName;
        this.isWildcard = methodSimpleName.equals(WILDCARD);
    }

    public MethodMatcher(String classSimpleName, String methodSimpleName) {
        this.classSimpleName = classSimpleName;
        this.methodSimpleName = methodSimpleName;
        this.isWildcard = methodSimpleName.equals(WILDCARD);
    }

    public boolean matches(MethodRepresentation methodRepresentation) {
        return (isWildcard || methodRepresentation.getMethodName().equals(methodSimpleName)) &&
                (methodRepresentation.getInterfacesSimpleClassNames().contains(classSimpleName) ||
                        methodRepresentation.getSuperClassesSimpleNames().contains(classSimpleName));
    }

    @Override
    public String toString() {
        return classSimpleName + "." + methodSimpleName;
    }
}