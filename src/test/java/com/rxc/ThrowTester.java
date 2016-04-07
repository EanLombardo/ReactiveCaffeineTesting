package com.rxc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ThrowTester {

    public static void assertThrows(final Runnable runnable, final Matcher<Throwable> throwableMatcher){
        try{
            runnable.run();
        } catch (final Throwable t){
            assertThat(t,throwableMatcher);
            return;
        }
        final Description description = new StringDescription();
        description.appendText("Expected: Runnable to throw throwable matching: ");
        description.appendDescriptionOf(throwableMatcher);
        description.appendText("But: Nothing was thrown");

        fail(description.toString());
    }
}
