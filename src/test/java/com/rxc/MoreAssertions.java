package com.rxc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MoreAssertions {

    public static void assertThrows(final ThrowingRunnable runnable, final Matcher<Throwable> throwableMatcher){
        try{
            runnable.run();
        } catch (final Throwable t){
            assertThat(t,throwableMatcher);
            return;
        }

        final Description description = new StringDescription();
        description.appendText("\nExpected: Runnable to throw throwable matching: ");
        description.appendDescriptionOf(throwableMatcher);
        description.appendText("\n     But: Nothing was thrown");

        fail(description.toString());
    }

    public static void assertTakesAtLeast(final ThrowingRunnable runnable, final long mills) throws Throwable{
        final long start = System.currentTimeMillis();
        runnable.run();
        final long duration = System.currentTimeMillis() - start;

        assertTrue("Execution did not take at least " + mills + "ms",duration >= mills);
    }
}
