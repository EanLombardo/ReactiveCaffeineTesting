package com.rxc.matchers;

import org.junit.Test;
import rx.Notification;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class OnCompletedMatcherTest {
    @Test
    public void testMatches() throws Exception {
        assertThat(Notification.createOnCompleted(),new OnCompletedMatcher());
    }

    @Test
    public void testNoMatch_notOnCompleted() throws Exception {
        assertThat(Notification.createOnCompleted(),not(new OnNextMatcher(containsString("fred"))));
        assertThat(Notification.createOnError(new Exception()),not(new OnNextMatcher(anything())));
    }
}