package com.rxc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import rx.Notification;
import rx.Subscriber;

import java.util.LinkedList;
import java.util.List;

public class TestSubscriber<T> extends Subscriber<T>{
    private final List<Notification<T>> notifications = new LinkedList<>();

    @Override
    public void onCompleted() {
        synchronized (notifications){
            notifications.add(Notification.<T>createOnCompleted());
        }
    }

    @Override
    public void onError(final Throwable e) {
        synchronized (notifications){
            notifications.add(Notification.<T>createOnError(e));
        }
    }

    @Override
    public void onNext(final T t) {
        synchronized (notifications){
            notifications.add(Notification.createOnNext(t));
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

    private void describeEventChain(final Description description){
        for(final Notification<T> notification : notifications){
            description.appendText("\n             ");
            NotificationDescriber.describeNotification(notification,description);
        }
        description.appendText("\n");
    }

    private void fail(final SelfDescribing expected, final String but){
        final Description description = new StringDescription();
        description.appendText("\n   Expected: ")
                .appendDescriptionOf(expected)
                .appendText("\n        but: ")
                .appendText(but)
                .appendText("\nEvent chain: ");
        describeEventChain(description);

        Assert.fail(description.toString());
    }

    public void assertHasEvent(final Matcher<Notification> matcher){
        if(!hasMatchingNotification(matcher)){
            fail(matcher,"There was no matching event in the event chain");
        }
    }
}
