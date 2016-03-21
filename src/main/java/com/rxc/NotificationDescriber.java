package com.rxc;

import org.hamcrest.Description;
import rx.Notification;

public class NotificationDescriber {

    /**
     * Describes a Notification to a given Description
     * @param notification The Notification to describe
     * @param description The Description to describe the Notification to
     */
    public static void describeNotification(final Notification notification, final Description description){
        switch (notification.getKind()){
            case OnNext:
                description.appendText("onNext(");
                description.appendValue(notification.getValue());
                description.appendText(")");
                return;
            case OnError:
                description.appendText("onError(");
                description.appendValue(notification.getThrowable());
                description.appendText(")");
                return;
            case OnCompleted:
                description.appendText("onCompleted()");
        }
    }
}
