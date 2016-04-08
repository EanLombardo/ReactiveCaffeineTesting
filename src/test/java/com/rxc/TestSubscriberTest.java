package com.rxc;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.rxc.MoreAssertions.assertTakesAtLeast;
import static com.rxc.MoreAssertions.assertThrows;
import static com.rxc.matchers.NotificationMatchers.isCompletion;
import static com.rxc.matchers.NotificationMatchers.isValue;
import static com.rxc.matchers.ThrowableMatchers.hasMessageThat;
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

        assertThrows(new ThrowingRunnable(){
            @Override
            public void run() {
                testSubscriber.assertHasEvent(isValue("glork"));
            }
        }, hasMessageThat(containsString("There was no matching event in the event")));
    }

    @Test
    public void awaitEvent_timesOut(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        Observable.<String>never().subscribe(testSubscriber);

        assertThrows(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testSubscriber.awaitEvent(isCompletion(),1,5000);
            }
        },hasMessageThat(containsString("Timed out waiting for event")));
    }

    @Test
    public void awaitEvent() throws Throwable{
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .sleep(2500)
                .emit("Glork")
                .build()
            .observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
        .subscribe(testSubscriber);

        assertTakesAtLeast(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testSubscriber.awaitEvent(isValue("Glork"),1,5000);
            }
        },2500);
    }
}