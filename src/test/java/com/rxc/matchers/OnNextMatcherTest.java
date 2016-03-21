package com.rxc.matchers;

import org.junit.Test;
import rx.Notification;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;


public class OnNextMatcherTest{

    @Test
    public void testMatches() throws Exception {
        assertThat(Notification.createOnNext(new Object()),new OnNextMatcher(anything()));
    }

    @Test
    public void testNoMatch_valueMatcherFails() throws Exception {
        assertThat(Notification.createOnNext("I am glork"),not(new OnNextMatcher(containsString("fred"))));
    }

    @Test
    public void testNoMatch_notOnNext() throws Exception {
        assertThat(Notification.createOnCompleted(),not(new OnNextMatcher(containsString("fred"))));
        assertThat(Notification.createOnError(new Exception()),not(new OnNextMatcher(anything())));
    }
}