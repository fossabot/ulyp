package com.ulyp.agent;

import com.test.cases.UserDefinedClassTestCases;
import com.ulyp.agent.util.MethodTraceTree;
import com.ulyp.agent.util.MethodTraceTreeNode;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;

public class UserDefinedClassInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldPrintEnumNames() {
        MethodTraceTree tree = executeClass(
                UserDefinedClassTestCases.class,
                "com.test.cases",
                "UserDefinedClassTestCases.returnInnerClass"
        );

        MethodTraceTreeNode root = tree.getRoot();

        assertThat(root.getResult(), matchesPattern("TestClass@\\d+"));
    }

    @Test
    public void shouldNotFailIfToStringCallsTracedMethod() {
        MethodTraceTree tree = executeClass(
                UserDefinedClassTestCases.class,
                "com.test.cases",
                "UserDefinedClassTestCases.returnClassThatCallsSelfInToString"
        );

        MethodTraceTreeNode root = tree.getRoot();

        assertThat(root.getResult(), is("ToStringCallsSelf{name='ToStringCallsSelf{name='n1', secondName='s1'}ToStringCallsSelf{name='n1', secondName='s1'}', secondName='ToStringCallsSelf{name='n1', secondName='s1'}/ToStringCallsSelf{name='n1', secondName='s1'}'}"));
    }
}