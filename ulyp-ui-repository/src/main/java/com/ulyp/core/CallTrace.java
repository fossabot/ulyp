package com.ulyp.core;

import com.ulyp.transport.BooleanType;
import com.ulyp.transport.TMethodDescriptionDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CallTrace {

    private long id;
    private String className;
    private String methodName;
    private boolean isVoidMethod;
    private List<ObjectValue> args;
    private List<String> parameterNames;
    private ObjectValue returnValue;
    private boolean thrown;
    private List<CallTrace> children;
    private int subtreeNodeCount;
    private CallGraphDatabase database;

    public CallTrace(
            List<ObjectValue> args,
            ObjectValue returnValue,
            boolean thrown,
            TMethodDescriptionDecoder methodDescription,
            List<CallTrace> children)
    {
        this.isVoidMethod = methodDescription.returnsSomething() == BooleanType.F;
        this.args = new ArrayList<>(args);
        this.returnValue = returnValue;
        this.thrown = thrown;
        int originalLimit = methodDescription.limit();

        TMethodDescriptionDecoder.ParameterNamesDecoder paramNamesDecoder = methodDescription.parameterNames();
        this.parameterNames = new ArrayList<>();
        while (paramNamesDecoder.hasNext()) {
            this.parameterNames.add(paramNamesDecoder.next().value());
        }
        this.className = methodDescription.className();
        this.methodName = methodDescription.methodName();
        methodDescription.limit(originalLimit);

        this.children = new ArrayList<>(children);
        this.subtreeNodeCount = children.stream().map(CallTrace::getSubtreeNodeCount).reduce(1, Integer::sum);
    }

    public CallGraphDatabase getDatabase() {
        return database;
    }

    public long getId() {
        return id;
    }

    public int getSubtreeNodeCount() {
        return subtreeNodeCount;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ObjectValue> getArgs() {
        return args;
    }

    public List<String> getArgTexts() {
        return args.stream().map(ObjectValue::getPrintedText).collect(Collectors.toList());
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public ObjectValue getReturnValue() {
        return returnValue;
    }

    public boolean hasThrown() {
        return thrown;
    }

    public List<CallTrace> getChildren() {
        return children;
    }

    public CallTrace setId(long id) {
        this.id = id;
        return this;
    }

    public void setDatabase(CallGraphDatabase database) {
        this.database = database;
    }

    public void delete() {
        database.deleteSubtree(id);
    }

    /**
     * @return either printed return value, or printed throwable if something was thrown
     */
    public String getResult() {
        return (isVoidMethod && !hasThrown()) ? "void" : returnValue.getPrintedText();
    }

    public String toString() {
        return getResult() + " : " +
                className +
                "." +
                methodName +
                args;
    }
}