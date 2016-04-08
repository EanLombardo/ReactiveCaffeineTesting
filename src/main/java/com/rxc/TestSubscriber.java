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

public class TestSubscriber<T> extends Subscriber<T>{
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

    private void fail(final SelfDescribing expected, final String but, final Notification<T> event){
        final Description description = new StringDescription();
        description
                .appendText("\n   Expected: ")
                .appendDescriptionOf(expected)
                .appendText("\n        but: ")
                .appendText(but)
                .appendText("\nevent chain: ");
        describeEventChain(description, event);

        Assert.fail(description.toString());
    }

    public void awaitEvent(final Matcher<Notification> matcher, final int times, final long timeout) throws InterruptedException {
        synchronized (notifications){
            if(hasMatchingNotification(matcher)){
                return;
            }

            currentWait = new AwaitContext(matcher,times,timeout);
        }

        currentWait.await();

        if(currentWait.didTimeout()){
            fail(matcher,"Timed out waiting for event");
        }
    }

    public AssertionChain beginAssertionChain(){
        return new AssertionChain(notifications.iterator());
    }

    public void assertHasEvent(final Matcher<Notification> matcher){
        if(!hasMatchingNotification(matcher)){
            fail(matcher,"There was no matching event in the event chain");
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

    public class AssertionChain {

        private Iterator<Notification<T>> iterator;

        AssertionChain(final Iterator<Notification<T>> iterator){
            this.iterator = iterator;
        }

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
    }

}
