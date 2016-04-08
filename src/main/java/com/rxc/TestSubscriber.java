package com.rxc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import rx.Notification;
import rx.Subscriber;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Subscriber} that records all events so that assertions can be made against them for testing purposes
 * TestSubscriber is entirely thread safe and makes no assumptions about the {@link rx.Observable} contract
 * @param <T> The type of values that will be emitted by the {@link rx.Observable}
 */
public class TestSubscriber<T> extends Subscriber<T>{
    private static final int DEFAULT_TIMEOUT = 5000;

    private final List<Notification<T>> notifications = new LinkedList<>();

    private AwaitContext currentWait = null;

    @Override
    public void onCompleted() {
        synchronized (notifications){
            handleEvent(Notification.<T>createOnCompleted());
        }
    }

    @Override
    public void onError(final Throwable e) {
        synchronized (notifications){
            handleEvent(Notification.<T>createOnError(e));
        }
    }

    @Override
    public void onNext(final T t) {
        synchronized (notifications){
            handleEvent(Notification.createOnNext(t));
        }
    }

    private void handleEvent(final Notification<T> event){
        notifications.add(event);

        if(currentWait != null){
            currentWait.handleEvent(event);
        }
    }

    private boolean hasMatchingNotification(final Matcher<Notification> matcher){
        for(final Notification<T> notification : notifications){
            if(matcher.matches(notification)){
                return true;
            }
        }

        return false;
    }

    private void describeEventChain(final Description description, final Notification<T> event){
        for(final Notification<T> notification : notifications){
            if(notification == event){
                description.appendText("\n    -------> ");
            } else {
                description.appendText("\n             ");
            }
            NotificationDescriber.describeNotification(notification,description);
        }
        description.appendText("\n");
    }

    private void fail(final SelfDescribing expected, final String but){
        fail(expected,but,null);
    }

    private void fail(final String expected, final String but, final Notification<T> event){
        final Description description = new StringDescription();
        description
                .appendText("\n   Expected: ")
                .appendText(expected)
                .appendText("\n        but: ")
                .appendText(but)
                .appendText("\nevent chain: ");
        describeEventChain(description, event);

        Assert.fail(description.toString());
    }

    private void fail(final SelfDescribing expected, final String but, final Notification<T> event){
        final Description description = new StringDescription();
        expected.describeTo(description);
        fail(expected.toString(),but,event);
    }

    /**
     * <p>Holds the current thread until the subscriber has received a certain number of events that match the given matcher</p>
     * <p>This is useful for waiting until a {@link rx.Observable} is in a certain state before making assumptions against it's events</p>
     * <p><b>Fails:</b> When the proper number of matching events aren't recorded before a timeout occurs</p>
     *
     * @param matcher The matcher used for checking the events
     * @param times How many events need to be seen that match before the thread is released
     * @param timeout How long to wait before timing out
     * @param timeUnit The unit to be used for the timeout
     * @throws InterruptedException When the thread is interrupted
     */
    public void awaitEvent(final Matcher<Notification> matcher, final int times, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        synchronized (notifications){
            if(hasMatchingNotification(matcher)){
                return;
            }

            currentWait = new AwaitContext(matcher,times,timeUnit.convert(timeout,TimeUnit.MILLISECONDS));
        }

        currentWait.await();

        if(currentWait.didTimeout()){
            fail(matcher,"Timed out waiting for event");
        }
    }

    /**
     * <p>Holds the current thread until the subscriber has received a certain number of events that match the given matcher</p>
     * <p>This is useful for waiting until a {@link rx.Observable} is in a certain state before making assumptions against it's events</p>
     * <p>The wait will timeout after {@value #DEFAULT_TIMEOUT}ms</p>
     * <p><b>Fails:</b> When the proper number of matching events aren't recorded before a timeout occurs</p>
     *
     * @param matcher The matcher used for checking the events
     * @param times How many events need to be seen that match before the thread is released
     * @throws InterruptedException When the thread is interrupted
     */
    public void awaitEvent(final Matcher<Notification> matcher, final int times) throws InterruptedException {
        awaitEvent(matcher,times,DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
    }

    /**
     * <p>Holds the current thread until the subscriber has received an events that match the given matcher</p>
     * <p>This is useful for waiting until a {@link rx.Observable} is in a certain state before making assumptions against it's events</p>
     * <p><b>Fails:</b> When a matching event isn't recorded before a timeout occurs</p>
     *
     * @param matcher The matcher used for checking the events
     * @param timeout How long to wait before timing out
     * @param timeUnit The unit to be used for the timeout
     * @throws InterruptedException When the thread is interrupted
     */
    public void awaitEvent(final Matcher<Notification> matcher,  final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        awaitEvent(matcher,1,timeout,timeUnit);
    }

    /**
     * <p>Holds the current thread until the subscriber has received an events that match the given matcher</p>
     * <p>This is useful for waiting until a {@link rx.Observable} is in a certain state before making assumptions against it's events</p>
     * <p>The wait will timeout after {@value #DEFAULT_TIMEOUT}ms</p>
     * <p><b>Fails:</b> When a matching event isn't recorded before a timeout occurs</p>
     *
     * @param matcher The matcher used for checking the events
     * @throws InterruptedException When the thread is interrupted
     *
     */
    public void awaitEvent(final Matcher<Notification> matcher) throws InterruptedException {
        awaitEvent(matcher,1,DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
    }

    /**
     * Begins a {@link AssertionChain} that will make assertions against all of the events that have been recorded
     * @return The AssertionChain to make AssertionsAgainst
     */
    public AssertionChain beginAssertionChain(){
        return new AssertionChain(notifications.iterator());
    }

    /**
     * Asserts that this Subscriber has received an event matching the given matcher
     * @param matcher The matcher to check with
     */
    public void assertHasEvent(final Matcher<Notification> matcher){
        if(!hasMatchingNotification(matcher)){
            fail(matcher,"There was no matching event in the event chain");
        }
    }

    /**
     * Asserts that this Subscriber has not received an event matching the given matcher
     * @param matcher The matcher to check with
     */
    public void assertDoesNotHaveEvent(final Matcher<Notification> matcher){
        for(final Notification<T> notification : notifications){
            if(matcher.matches(notification)){
                final Description description = new StringDescription();
                description.appendText("no event matching: ");
                description.appendDescriptionOf(matcher);

                fail(description.toString(),"There was a matching event",notification);
            }
        }
    }

    private final class AwaitContext{
        private final Matcher<Notification> matcher;
        private final long timeout;

        private final CountDownLatch countDownLatch;

        AwaitContext(final Matcher<Notification> matcher, final int times, final long timeout){
            this.matcher = matcher;
            this.timeout = timeout;

            countDownLatch = new CountDownLatch(times);
        }

        private void handleEvent(final Notification notification){
            if(matcher.matches(notification)){
                countDownLatch.countDown();
            }
        }

        private void await() throws InterruptedException {
            countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        }

        private boolean didTimeout(){
            return countDownLatch.getCount() != 0;
        }
    }

    /**
     * <p>A fluent class used to walk the chain of events received by a {@link TestSubscriber} and make assertions against each event in the order received<</p>
     * <p>AssertionChain does not make assertions against events as they come in, instead it asserts against the events received by the {@link TestSubscriber} when
     * {@link TestSubscriber#beginAssertionChain()} was called. This means in multi threaded situations you will likely have to use {@link TestSubscriber#awaitEvent(Matcher)}
     * to wait for the {@link TestSubscriber} to receive the events that you want to assert against.</p>
     */
    public class AssertionChain {

        private Iterator<Notification<T>> iterator;

        AssertionChain(final Iterator<Notification<T>> iterator){
            this.iterator = iterator;
        }

        /**
         * <p>Asserts that the next event matches the given matcher</p>
         * <p><b>Fails:</b> When the next event does not match, or when there is no next event</p>
         * @param eventMatcher The matcher to check with
         * @return the AssertionChain to continue to make assertions against
         */
        public AssertionChain assertNextEvent(final Matcher<Notification> eventMatcher){
            if(iterator.hasNext()){
                final Notification<T> event = iterator.next();
                if(!eventMatcher.matches(event)){
                    final Description description = new StringDescription();
                    eventMatcher.describeMismatch(event,description);
                    fail(eventMatcher,description.toString(),event);
                }
            } else {
                fail(eventMatcher,"There were no remaining events");
            }

            return this;
        }

        /**
         * <p>Skips the next event in the recorded event chain</p>
         * <p>This will not fail a test under any circumstances as this should eventually be followed by a assertNextEvent that would fail if there were no more events</p>
         * @return the AssertionChain to continue to make assertions against
         */
        public AssertionChain ignoreNextEvent(){
            if(iterator.hasNext()){
                iterator.next();
            }

            return this;
        }

        /**
         * <p>Skips a given amount of events in the recorded event chain</p>
         * <p>This will not fail a test under any circumstances as this should eventually be followed by a assertNextEvent that would fail if there were no more events</p>
         * @return the AssertionChain to continue to make assertions against
         */
        public AssertionChain ignoreNextEvents(final int count){
            int curr = 0;
            while (curr < count && iterator.hasNext()){
                iterator.next();
                curr++;
            }

            return this;
        }

        /**
         * <p>Walks through the event chain skipping events until the given number of events have been reached that match the given matcher</p>
         * <p>This does skip the last event matched, meaning that the next event will be the event immediately after the last matching event</p>
         * <p><b>Fails:</b> When all events have been checked but not enough matching events were found</p>
         * @param matcher The matcher to check with
         * @param times How many matching events to skip
         * @return the AssertionChain to continue to make assertions against
         */
        public AssertionChain ignoreUntilEvent(final Matcher<Notification> matcher, final int times){
            int curr = 0;

            while (curr < times){
                if(iterator.hasNext()){
                    if(matcher.matches(iterator.next())) {
                        curr++;
                    }
                } else {
                    fail(matcher,"There was no such event");
                }
            }

            return this;
        }

        /**
         * <p>Walks through the event chain skipping events until an event has been reached that matches the given matcher</p>
         * <p>This does skip the event matched, meaning that the next event will be the event immediately after the matching event</p>
         * <p><b>Fails:</b> When all events have been checked but not enough matching events were found</p>
         * @param matcher The matcher to check with
         * @return the AssertionChain to continue to make assertions against
         */
        public AssertionChain ignoreUntilEvent(final Matcher<Notification> matcher){
            return ignoreUntilEvent(matcher,1);
        }
    }

}
