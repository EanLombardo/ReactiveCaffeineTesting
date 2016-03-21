package com.rxc.matchers;


import org.hamcrest.Matcher;
import rx.Notification;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;

public class NotificationMatchers {

    /**
     * A matcher that matches OnNext notifications with a value matching the given matcher
     * @param valueMatcher The matcher used to match the value of the OnNext Notification
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

    /**
     * A matcher that matches OnError notifications with a Throwable matching the given matcher
     * @param errorMatcher The matcher used to match the Throwable of the OnError Notification
     */
    public static Matcher<Notification> isErrorThat(final Matcher<? extends Throwable> errorMatcher){
        return new OnErrorMatcher(errorMatcher);
    }

    /**
     * A matcher that matches OnError notifications with a Throwable that is an instance of the given Class
     * @param errorClass The Class used to match the onError Notification's Throwable
     */
    public static Matcher<Notification> isError(final Class<? extends Throwable> errorClass){
        return isErrorThat(isA(errorClass));
    }
}
