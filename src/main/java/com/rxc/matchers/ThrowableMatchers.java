package com.rxc.matchers;


import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class ThrowableMatchers {

    public final Matcher<Throwable> hasMessageThat(final Matcher<String> stringMatcher){
       return new FeatureMatcher<Throwable, String>(stringMatcher,"message","message") {
           @Override
           protected String featureValueOf(Throwable actual) {
               return actual.getMessage();
           }
       };
    }

    public final Matcher<Throwable> hasCauseThat(final Matcher<Throwable> throwableMatcher){
        return new FeatureMatcher<Throwable, Throwable>(throwableMatcher,"cause","cause") {
            @Override
            protected Throwable featureValueOf(Throwable actual) {
                return actual.getCause();
            }
        };
    }
}
