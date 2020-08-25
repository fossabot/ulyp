package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.TestSettingsBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.*;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ObjectArrayInstrumentationTest extends AbstractInstrumentationTest {

    public static class TakesEmptyObjectArray {

        public static void main(String[] args) {
            new TakesEmptyObjectArray().accept(new Object[]{});
        }

        public void accept(Object[] array) {
        }
    }

    @Test
    public void shouldProvideArgumentTypes() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(TakesEmptyObjectArray.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRepresentation objectRepresentation = (ObjectArrayRepresentation) root.getArgs().get(0);


        assertThat(objectRepresentation.getLength(), is(0));
        assertThat(objectRepresentation.getRecordedItems(), Matchers.empty());
    }

    public static class TakesStringArrayWithSomeString {

        public static void main(String[] args) {
            new TakesStringArrayWithSomeString().accept(new String[]{
                    "sddsad",
                    "zx",
                    "sdsd"
            });
        }

        public void accept(String[] array) {
        }
    }

    @Test
    public void shouldRecordSimpleArrayWithString() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(TakesStringArrayWithSomeString.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRepresentation objectRepresentation = (ObjectArrayRepresentation) root.getArgs().get(0);


        assertThat(objectRepresentation.getLength(), is(3));

        List<ObjectRepresentation> items = objectRepresentation.getRecordedItems();

        assertThat(items, Matchers.hasSize(3));

        StringObjectRepresentation str0 = (StringObjectRepresentation) items.get(0);
        assertEquals(str0.getPrintedText(), "sddsad");

        StringObjectRepresentation str1 = (StringObjectRepresentation) items.get(1);
        assertEquals(str1.getPrintedText(), "zx");

        StringObjectRepresentation str2 = (StringObjectRepresentation) items.get(2);
        assertEquals(str2.getPrintedText(), "sdsd`");
    }

    public static class TakesVariousItemsArray {

        public static void main(String[] args) {
            new TakesVariousItemsArray().accept(new Object[]{
                    new X(),
                    664,
                    "asdd",
                    new X(),
                    Object.class
            });
        }

        public void accept(Object[] array) {
        }
    }

    private static class X {
        public X() {
        }
    }

    @Test
    public void testUserDefinedClassArrayWith3Elements() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(TakesVariousItemsArray.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRepresentation objectRepresentation = (ObjectArrayRepresentation) root.getArgs().get(0);

        assertThat(objectRepresentation.getLength(), is(5));

        List<ObjectRepresentation> items = objectRepresentation.getRecordedItems();

        IdentityObjectRepresentation arg0 = (IdentityObjectRepresentation) items.get(0);
        assertThat(arg0.getType().getName(), Matchers.is(X.class.getName()));

        NumberObjectRepresentation arg1 = (NumberObjectRepresentation) items.get(1);
        assertThat(arg1.getPrintedText(), is("664"));
    }
}
