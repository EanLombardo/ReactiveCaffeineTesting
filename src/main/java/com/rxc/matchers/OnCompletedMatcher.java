package com.rxc.matchers;

import com.rxc.NotificationDescriber;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import rx.Notification;

/**
 * A matcher that matches onCompleted Notifications
 */
public class OnCompletedMatcher extends TypeSafeMatcher<Notification>{
    @Override
    protected boolean matchesSafely(final Notification item) {
        return item.getKind() == Notification.Kind.OnCompleted;
    }
    @Override
    public void describeTo(final Description description) {
        description.appendText("onCompleted()");
    }

    @Override
    protected void describeMismatchSafely(Notification item, Description mismatchDescription) {
        mismatchDescription.appendText("was ");
        NotificationDescriber.describeNotification(item,mismatchDescription);
    }
}
