package com.rxc.matchers;


import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class ThrowableMatchers {

    /**
     * A {@link Matcher} that matches a Throwable with a message that matches a given matcher
     * @param stringMatcher The matcher to check the Throwable message against
     * @return the matcher
     */
    public static Matcher<Throwable> hasMessageThat(final Matcher<String> stringMatcher){
       return new FeatureMatcher<Throwable, String>(stringMatcher,"message","message") {
           @Override
           protected String featureValueOf(Throwable actual) {
               return actual.getMessage();
           }
       };
    }

    /**
     * A {@link Matcher} that matches a Throwable with a cause that matches a given matcher
     * @param throwableMatcher The matcher to check the Throwable cause against
     * @return the matcher
     */
    public static Matcher<Throwable> hasCauseThat(final Matcher<Throwable> throwableMatcher){
        return new FeatureMatcher<Throwable, Throwable>(throwableMatcher,"cause","cause") {
            @Override
            protected Throwable featureValueOf(Throwable actual) {
                return actual.getCause();
            }
        };
    }
}
