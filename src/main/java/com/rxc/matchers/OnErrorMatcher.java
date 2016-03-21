package com.rxc.matchers;

import com.rxc.NotificationDescriber;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import rx.Notification;

/**
 * A matcher that matches onError Notifications with an error matching the given matcher
 */
public class OnErrorMatcher extends TypeSafeMatcher<Notification> {

    private final Matcher<? extends Throwable> errorMatcher;

    /**
     * Constructs the matcher
     * @param errorMatcher The matcher used to match the error of the onError Notification
     */
    public OnErrorMatcher(final Matcher<? extends Throwable> errorMatcher) {
        this.errorMatcher = errorMatcher;
    }

    @Override
    protected boolean matchesSafely(final Notification item) {
        return item.getKind() == Notification.Kind.OnError && errorMatcher.matches(item.getThrowable());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("onError with throwable matching :")
                .appendDescriptionOf(errorMatcher);
    }

    @Override
    protected void describeMismatchSafely(Notification item, Description mismatchDescription) {
        mismatchDescription.appendText("was ");
        NotificationDescriber.describeNotification(item,mismatchDescription);
    }
}
