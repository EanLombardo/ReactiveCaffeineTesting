package com.rxc.matchers;

import org.junit.Test;
import rx.Notification;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class OnErrorMatcherTest {
    @Test
    public void testMatches() throws Exception {
        assertThat(Notification.createOnError(new Exception()),new OnErrorMatcher(isA(Throwable.class)));
    }

    @Test
    public void testNoMatch_valueMatcherFails() throws Exception {
        assertThat(Notification.createOnError(new Exception()),not(new OnNextMatcher(not(isA(Throwable.class)))));
    }

    @Test
    public void testNoMatch_notOnError() throws Exception {
        assertThat(Notification.createOnCompleted(),not(new OnNextMatcher(isA(Throwable.class))));
        assertThat(Notification.createOnNext(new Object()),not(new OnNextMatcher(isA(Throwable.class))));
    }
}