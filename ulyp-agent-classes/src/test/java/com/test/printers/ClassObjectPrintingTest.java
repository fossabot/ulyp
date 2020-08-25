package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.TestSettingsBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.ClassObjectRepresentation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassObjectPrintingTest extends AbstractInstrumentationTest {

    @Test
    public void testClassTypeReturning() {

        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("returnClass")
        );

        ClassObjectRepresentation arg = (ClassObjectRepresentation) root.getReturnValue();

        assertEquals(X.class.getName(), arg.getCarriedType().getName());
    }

    @Test
    public void testClassTypePassing() {

        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(PassClazz.class)
                        .setMethodToRecord("pass")
        );

        ClassObjectRepresentation arg = (ClassObjectRepresentation) root.getArgs().get(0);

        assertEquals(X.class.getName(), arg.getCarriedType().getName());
    }

    static class X {
    }

    static class PassClazz {

        public static Class<?> returnClass() {
            return X.class;
        }

        public static void pass(Class<?> clazz) {
            System.out.println(clazz);
        }

        public static void main(String[] args) {
            pass(X.class);
            System.out.println(returnClass());
        }
    }
}
