package com.rxc;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static com.rxc.MoreAssertions.assertTakesAtLeast;
import static com.rxc.MoreAssertions.assertThrows;
import static com.rxc.matchers.NotificationMatchers.*;
import static com.rxc.matchers.ThrowableMatchers.hasMessageThat;
import static org.hamcrest.CoreMatchers.*;

public class TestSubscriberTest {

    @Test
    public void hasEvent() throws Exception {
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
                testSubscriber.awaitEvent(isCompletion(),1,5000,TimeUnit.MILLISECONDS);
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
                testSubscriber.awaitEvent(isValue("Glork"),1,5000, TimeUnit.MILLISECONDS);
            }
        },2500);
    }

    @Test
    public void assertionChain_assertNextEvent(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
        .subscribe(testSubscriber);

        testSubscriber.beginAssertionChain()
                      .assertNextEvent(isValue("Glork"))
                      .assertNextEvent(isValueThat(containsString("ork")))
                      .assertNextEvent(isValueThat(endsWith("k")))
                      .assertNextEvent(isValue("spoon"))
                      .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
    }

    @Test
    public void assertionChain_assertNextEvent_failsNotMatching(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        assertThrows(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testSubscriber.beginAssertionChain()
                        .assertNextEvent(isValue("Glork"))
                        .assertNextEvent(isValueThat(containsString("ork")))
                        .assertNextEvent(isValueThat(endsWith("k")))
                        .assertNextEvent(isValue("fmoiefn"))
                        .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
            }
        }, hasMessageThat(containsString("was onNext(\"spoon\")")));
    }

    @Test
    public void assertionChain_assertNextEvent_failsOutOfEvents(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        assertThrows(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testSubscriber.beginAssertionChain()
                        .assertNextEvent(isValue("Glork"))
                        .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))))
                        .assertNextEvent(isValue("Glork"));
            }
        }, hasMessageThat(containsString("There were no remaining events")));
    }

    @Test
    public void assertionChain_ignoreNextEvent(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        testSubscriber.beginAssertionChain()
                .assertNextEvent(isValue("Glork"))
                .ignoreNextEvent()
                .assertNextEvent(isValueThat(startsWith("fo")))
                .assertNextEvent(isValue("spoon"))
                .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
    }

    @Test
    public void assertionChain_ignoreNextEvents(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        testSubscriber.beginAssertionChain()
                .ignoreNextEvents(4)
                .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
    }

    @Test
    public void assertionChain_ignoreUntil(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        testSubscriber.beginAssertionChain()
                .ignoreUntilEvent(isValueThat(endsWith("spoon")),1)
                .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
    }

    @Test
    public void assertionChain_ignoreUntil_fails(){
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                .emit("Glork")
                .emit("flork")
                .emit("fork")
                .emit("spoon")
                .error(new Exception("There is no spoon"))
                .subscribe(testSubscriber);

        assertThrows(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                testSubscriber.beginAssertionChain()
                        .ignoreUntilEvent(isValueThat(endsWith("chicken")), 1);
            }
        }, hasMessageThat(containsString("There was no such event")));
    }
}