package com.rxc.matchers;


import org.hamcrest.Matcher;
import rx.Notification;

import static org.hamcrest.core.Is.is;

public class NotificationMatchers {

    /**
     * A matcher that matches OnNext notifications with a value matching the given matcher
     * @param valueMatcher The matcher used to match the value of the OnNextNotification
     */
    public static Matcher<Notification> isValueThat(final Matcher valueMatcher){
        return new OnNextMatcher(valueMatcher);
    }

    /**
     * A matcher that matches OnNext notifications with a value equal to the given value
     * @param value The value compared to the value of the OnNext notification
     */
    public static Matcher<Notification> isValue(final Object value){
        return isValueThat(is(value));
    }
}
