package com.rxc;

import com.rxc.matchers.ThrowableMatchers;
import org.junit.Test;
import rx.Observable;

import static com.rxc.ThrowTester.assertThrows;
import static com.rxc.matchers.NotificationMatchers.isValue;
import static org.hamcrest.CoreMatchers.containsString;

public class TestSubscriberTest {

    @Test
    public void hasEvent_matches() throws Exception {
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        Observable.just("hello").subscribe(testSubscriber);

        testSubscriber.assertHasEvent(isValue("hello"));
    }

    @Test
    public void hasEvent_doesntMatch() throws Exception {
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        Observable.just("hello").subscribe(testSubscriber);

        assertThrows(new Runnable(){
            @Override
            public void run() {
                testSubscriber.assertHasEvent(isValue("glork"));
            }
        }, ThrowableMatchers.hasMessageThat(containsString("There was no matching event in the event")));

    }
}