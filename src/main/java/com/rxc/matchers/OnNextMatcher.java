package com.rxc.matchers;

import com.rxc.NotificationDescriber;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import rx.Notification;

/**
 * A matcher that matches OnNext notifications with a value matching the given matcher
 */
public class OnNextMatcher extends TypeSafeMatcher<Notification> {

    private final Matcher valueMatcher;

    /**
     * Constructs the matcher
     * @param valueMatcher The matcher used to match the value of the onNext Notification
     */
    public OnNextMatcher(final Matcher valueMatcher) {
        this.valueMatcher = valueMatcher;
    }


    @Override
    public void describeTo(final Description description) {
        description.appendText("onNext with value matching: ")
                   .appendDescriptionOf(valueMatcher);
    }

    @Override
    protected boolean matchesSafely(final Notification item) {
        return item.getKind() == Notification.Kind.OnNext && valueMatcher.matches(item.getValue());
    }

    @Override
    protected void describeMismatchSafely(final Notification item, final Description mismatchDescription) {
        mismatchDescription.appendText("was ");
        NotificationDescriber.describeNotification(item,mismatchDescription);
    }
}
