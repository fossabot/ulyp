package com.ulyp.agent;

import com.ulyp.agent.tests.AtomicNumbersTestCases;
import com.ulyp.agent.transport.MethodTraceTree;
import com.ulyp.agent.transport.MethodTraceTreeBuilder;
import com.ulyp.agent.transport.MethodTraceTreeNode;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AtomicNumbersTest extends AbstractInstrumentationTest {

    @Test
    public void testAtomicIntegerSum() {
        MethodTraceTree tree = MethodTraceTreeBuilder.from(executeClass(
                AtomicNumbersTestCases.class,
                "com.ulyp.agent.tests",
                "AtomicNumbersTestCases.intSum"
        ));

        MethodTraceTreeNode root = tree.getRoot();

        assertThat(root.getArgs(), is(Arrays.asList("-234", "23")));
        assertThat(root.getReturnValue(), is("-211"));
    }

    @Test
    public void testBoxedDoubleSum() {
        MethodTraceTree tree = MethodTraceTreeBuilder.from(executeClass(
                AtomicNumbersTestCases.class,
                "com.ulyp.agent.tests",
                "AtomicNumbersTestCases.longSum"
        ));

        MethodTraceTreeNode root = tree.getRoot();

        assertThat(root.getArgs(), is(Arrays.asList("-234", "23")));
        assertThat(root.getReturnValue(), is("-211"));
    }
}
